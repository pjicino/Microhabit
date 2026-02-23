package com.microhabit.app.viewmodel

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.microhabit.app.data.HabitDatabase
import com.microhabit.app.data.HabitRecord
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

// DataStore 扩展属性，保存熔断状态
val android.content.Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "meltdown_prefs")

val MELTDOWN_ACTIVE_KEY = booleanPreferencesKey("isMeltdownActive")
val MELTDOWN_TIME_KEY = longPreferencesKey("meltdownTimestamp")

/**
 * 触发熔断的状态词集合
 * 设计理念：只有"空转"和"麻木"才代表神经系统真正耗竭
 */
val MELTDOWN_STATE_WORDS = setOf("木了", "空了")
const val MELTDOWN_DAYS = 5               // 连续触发天数阈值
const val MELTDOWN_SCORE_THRESHOLD = 2    // 能量分数阈值（严格低于此值）
const val MELTDOWN_DURATION_HOURS = 48L   // 熔断持续时长（小时）

data class MeltdownState(
    val isActive: Boolean = false,
    val triggeredAt: Long = 0L,
    val hoursRemaining: Long = 0L
)

data class UiState(
    val meltdown: MeltdownState = MeltdownState(),
    val showRecordSheet: Boolean = false,
    val currentAnchor: String = "",
    val submitSuccess: Boolean = false
)

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = HabitDatabase.getDatabase(application).habitDao()
    private val dataStore = application.dataStore

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        // 启动时从 DataStore 恢复熔断状态，判断是否已过期
        viewModelScope.launch {
            dataStore.data.collect { prefs ->
                val isActive = prefs[MELTDOWN_ACTIVE_KEY] ?: false
                val triggeredAt = prefs[MELTDOWN_TIME_KEY] ?: 0L

                if (isActive) {
                    val elapsed = System.currentTimeMillis() - triggeredAt
                    val elapsedHours = TimeUnit.MILLISECONDS.toHours(elapsed)

                    if (elapsedHours >= MELTDOWN_DURATION_HOURS) {
                        // 48 小时已过，自动解除熔断
                        clearMeltdown()
                    } else {
                        val remaining = MELTDOWN_DURATION_HOURS - elapsedHours
                        _uiState.update {
                            it.copy(meltdown = MeltdownState(true, triggeredAt, remaining))
                        }
                    }
                }
            }
        }
    }

    fun openRecordSheet(anchor: String) {
        _uiState.update { it.copy(showRecordSheet = true, currentAnchor = anchor, submitSuccess = false) }
    }

    fun closeRecordSheet() {
        _uiState.update { it.copy(showRecordSheet = false, submitSuccess = false) }
    }

    fun submitRecord(
        anchorType: String,
        stateWord: String,
        energyScore: Int,
        thought: String?
    ) {
        viewModelScope.launch {
            dao.insert(
                HabitRecord(
                    anchorType = anchorType,
                    stateWord = stateWord,
                    energyScore = energyScore,
                    thought = thought
                )
            )
            // 每次提交后检查熔断条件
            checkMeltdownCondition()
            _uiState.update { it.copy(submitSuccess = true) }
        }
    }

    /**
     * 熔断机制核心逻辑
     *
     * 触发条件：最近 5 天内的所有记录，状态词均包含"木了"或"空了"，且能量分数均低于 2 分。
     *
     * 设计说明：不严格要求"连续天"，而是看最近 5 天内的记录整体趋势，
     * 因为高压人群本就可能偶尔漏打卡，但整体状态持续低迷更能说明问题。
     */
    private suspend fun checkMeltdownCondition() {
        val fiveDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(MELTDOWN_DAYS.toLong())

        dao.getRecordsSince(fiveDaysAgo).firstOrNull()?.let { records ->
            // 记录数不足，暂不触发
            if (records.size < MELTDOWN_DAYS) return

            val allMatch = records.all { record ->
                record.stateWord in MELTDOWN_STATE_WORDS && record.energyScore < MELTDOWN_SCORE_THRESHOLD
            }

            if (allMatch) triggerMeltdown()
        }
    }

    private suspend fun triggerMeltdown() {
        val now = System.currentTimeMillis()
        dataStore.edit { prefs ->
            prefs[MELTDOWN_ACTIVE_KEY] = true
            prefs[MELTDOWN_TIME_KEY] = now
        }
        _uiState.update {
            it.copy(meltdown = MeltdownState(true, now, MELTDOWN_DURATION_HOURS))
        }
    }

    private suspend fun clearMeltdown() {
        dataStore.edit { prefs ->
            prefs[MELTDOWN_ACTIVE_KEY] = false
            prefs[MELTDOWN_TIME_KEY] = 0L
        }
        _uiState.update { it.copy(meltdown = MeltdownState()) }
    }
}
