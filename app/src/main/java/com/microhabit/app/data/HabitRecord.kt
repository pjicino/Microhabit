package com.microhabit.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 习惯记录实体
 * 设计理念：字段最小化，只记录神经系统修复所需的关键信息
 */
@Entity(tableName = "habit_records")
data class HabitRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    // 锚点类型：起床锚点 / 下班锚点 / 睡前锚点
    val anchorType: String,
    // 状态词：用户当前的神经系统感受
    val stateWord: String,
    // 能量评分：1-5 分
    val energyScore: Int,
    // 睡前念头（仅睡前锚点填写，可为空）
    val thought: String? = null
)
