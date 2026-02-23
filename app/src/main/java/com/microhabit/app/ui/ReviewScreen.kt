package com.microhabit.app.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.microhabit.app.data.HabitRecord
import com.microhabit.app.viewmodel.HabitViewModel
import com.microhabit.app.viewmodel.ReviewDay
import java.text.SimpleDateFormat
import java.util.*

// 复盘页面有三个 Tab：日历视图 / 周总结 / 全部记录
enum class ReviewTab(val label: String) { Calendar("日历"), Weekly("周总结"), All("全部") }

@Composable
fun ReviewScreen(vm: HabitViewModel, modifier: Modifier = Modifier) {
    val state by vm.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(ReviewTab.Calendar) }

    if (state.reviewDays.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📖", fontSize = 48.sp)
                Spacer(Modifier.height(12.dp))
                Text("还没有记录", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
            }
        }
        return
    }

    Column(modifier = modifier.fillMaxSize()) {
        // 标题 + Tab
        Column(modifier = Modifier.padding(horizontal = 20.dp).padding(top = 16.dp, bottom = 8.dp)) {
            Text("复盘", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            // 分段选择器
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ReviewTab.entries.forEach { tab ->
                    val selected = selectedTab == tab
                    Box(
                        modifier = Modifier.weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { selectedTab = tab }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            tab.label, fontSize = 14.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        when (selectedTab) {
            ReviewTab.Calendar -> CalendarView(reviewDays = state.reviewDays, allRecords = state.allRecords)
            ReviewTab.Weekly   -> WeeklySummaryView(reviewDays = state.reviewDays)
            ReviewTab.All      -> AllRecordsView(reviewDays = state.reviewDays)
        }
    }
}

// ── 日历视图 ──────────────────────────────────────────────

@Composable
fun CalendarView(reviewDays: List<ReviewDay>, allRecords: List<HabitRecord>) {
    val today = Calendar.getInstance()
    var displayMonth by remember { mutableIntStateOf(today.get(Calendar.MONTH)) }
    var displayYear by remember { mutableIntStateOf(today.get(Calendar.YEAR)) }
    var selectedDate by remember { mutableStateOf<String?>(null) }

    // 哪些日期有记录
    val recordedDates = remember(allRecords) {
        allRecords.map {
            SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE).format(Date(it.timestamp))
        }.toSet()
    }
    // 选中日期的记录
    val selectedRecords = remember(selectedDate, allRecords) {
        if (selectedDate == null) emptyList()
        else allRecords.filter {
            SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE).format(Date(it.timestamp)) == selectedDate
        }.sortedByDescending { it.timestamp }
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // 月份导航
            Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(1.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = {
                            if (displayMonth == 0) { displayMonth = 11; displayYear-- }
                            else displayMonth--
                            selectedDate = null
                        }) { Icon(Icons.Filled.ChevronLeft, null) }

                        Text(
                            "${displayYear}年 ${displayMonth + 1}月",
                            fontSize = 16.sp, fontWeight = FontWeight.SemiBold
                        )

                        IconButton(onClick = {
                            val now = Calendar.getInstance()
                            if (displayYear < now.get(Calendar.YEAR) ||
                                (displayYear == now.get(Calendar.YEAR) && displayMonth < now.get(Calendar.MONTH))) {
                                if (displayMonth == 11) { displayMonth = 0; displayYear++ }
                                else displayMonth++
                                selectedDate = null
                            }
                        }) { Icon(Icons.Filled.ChevronRight, null) }
                    }

                    Spacer(Modifier.height(8.dp))

                    // 星期行
                    val weekdays = listOf("日", "一", "二", "三", "四", "五", "六")
                    Row(Modifier.fillMaxWidth()) {
                        weekdays.forEach { d ->
                            Text(d, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.45f))
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    // 日期格子
                    val cal = Calendar.getInstance().apply {
                        set(Calendar.YEAR, displayYear)
                        set(Calendar.MONTH, displayMonth)
                        set(Calendar.DAY_OF_MONTH, 1)
                    }
                    val firstDow = cal.get(Calendar.DAY_OF_WEEK) - 1
                    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE).format(Date())

                    val cells = firstDow + daysInMonth
                    val rows = (cells + 6) / 7

                    for (row in 0 until rows) {
                        Row(Modifier.fillMaxWidth()) {
                            for (col in 0 until 7) {
                                val dayNum = row * 7 + col - firstDow + 1
                                if (dayNum < 1 || dayNum > daysInMonth) {
                                    Box(Modifier.weight(1f).aspectRatio(1f))
                                } else {
                                    val dateStr = "%04d-%02d-%02d".format(displayYear, displayMonth + 1, dayNum)
                                    val hasRecord = dateStr in recordedDates
                                    val isToday = dateStr == todayStr
                                    val isSelected = dateStr == selectedDate

                                    Box(
                                        modifier = Modifier.weight(1f).aspectRatio(1f)
                                            .padding(2.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isSelected -> MaterialTheme.colorScheme.primary
                                                    isToday -> MaterialTheme.colorScheme.primaryContainer
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .clickable(enabled = hasRecord) { selectedDate = if (isSelected) null else dateStr },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                "$dayNum",
                                                fontSize = 13.sp,
                                                color = when {
                                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                                    hasRecord -> MaterialTheme.colorScheme.primary
                                                    else -> MaterialTheme.colorScheme.onSurface.copy(0.45f)
                                                },
                                                fontWeight = if (hasRecord) FontWeight.SemiBold else FontWeight.Normal
                                            )
                                            // 有记录的日期显示小圆点
                                            if (hasRecord && !isSelected) {
                                                Box(
                                                    Modifier.size(4.dp)
                                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 选中日期的记录详情
        if (selectedDate != null && selectedRecords.isNotEmpty()) {
            item {
                Text("${selectedDate!!.substring(5).replace("-", "月")}日 的记录",
                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
            }
            items(selectedRecords) { record ->
                RecordCard(record = record)
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

// ── 周总结视图 ────────────────────────────────────────────

@Composable
fun WeeklySummaryView(reviewDays: List<ReviewDay>) {
    // 按周分组
    val weekGroups = remember(reviewDays) {
        groupByWeek(reviewDays)
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(weekGroups) { (weekLabel, days) ->
            WeekSummaryCard(weekLabel = weekLabel, days = days)
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

data class WeekGroup(val weekLabel: String, val days: List<ReviewDay>)

fun groupByWeek(reviewDays: List<ReviewDay>): List<WeekGroup> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE)
    val groups = mutableMapOf<String, MutableList<ReviewDay>>()

    reviewDays.forEach { day ->
        // 从 dateLabel 还原日期字符串（近似处理）
        val cal = Calendar.getInstance()
        // 找到该 ReviewDay 第一条记录的时间戳
        val ts = day.records.firstOrNull()?.timestamp ?: return@forEach
        cal.timeInMillis = ts
        // 本周周一
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val weekStart = sdf.format(cal.time)
        cal.add(Calendar.DAY_OF_WEEK, 6)
        val weekEnd = sdf.format(cal.time)
        val key = "$weekStart ~ $weekEnd"
        groups.getOrPut(key) { mutableListOf() }.add(day)
    }

    return groups.entries
        .sortedByDescending { it.key }
        .map { WeekGroup(it.key, it.value) }
}

@Composable
fun WeekSummaryCard(weekLabel: String, days: List<ReviewDay>) {
    val allRecords = days.flatMap { it.records }
    val avgEnergy = if (allRecords.isEmpty()) 0f else allRecords.map { it.energyScore }.average().toFloat()
    val totalCount = allRecords.size
    val topWord = allRecords.groupBy { it.stateWord }.maxByOrNull { it.value.size }?.key ?: "-"
    val activeDays = days.size

    // 自动生成周总结文字
    val summary = buildWeeklySummary(avgEnergy, topWord, activeDays, totalCount)

    var expanded by remember { mutableStateOf(true) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(weekLabel, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("$activeDays 天打卡  $totalCount 条记录  均能量 ${"%.1f".format(avgEnergy)}",
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.45f))
                }
                Icon(if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    null, tint = MaterialTheme.colorScheme.onSurface.copy(0.4f))
            }

            AnimatedVisibility(visible = expanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 14.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.4f))
                    Spacer(Modifier.height(12.dp))

                    // 数字概览行
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MiniStat("最多状态词", topWord, Modifier.weight(1f))
                        MiniStat("平均能量", "%.1f".format(avgEnergy), Modifier.weight(1f),
                            color = if (avgEnergy < 2f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                        MiniStat("活跃天数", "$activeDays 天", Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(12.dp))

                    // 文字总结
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            summary, fontSize = 13.sp, lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.75f),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MiniStat(label: String, value: String, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurface) {
    Surface(modifier = modifier, shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color, textAlign = TextAlign.Center)
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f), textAlign = TextAlign.Center)
        }
    }
}

fun buildWeeklySummary(avgEnergy: Float, topWord: String, activeDays: Int, totalCount: Int): String {
    val suppress = setOf("木了", "空了", "软了", "沉了", "发懵")
    val activate = setOf("紧绷", "焦灼", "亢奋", "烦躁")
    val freqDesc = when {
        activeDays >= 6 -> "这周打卡非常规律"
        activeDays >= 4 -> "这周保持了不错的记录频率"
        activeDays >= 2 -> "这周记录不算规律"
        else -> "这周只记录了 $activeDays 天"
    }
    val stateDesc = when {
        topWord in suppress && avgEnergy < 2f -> "整体状态以抑制为主，能量偏低，神经系统需要主动修复。"
        topWord in suppress -> "出现较多抑制感，但能量尚可，注意防止麻木蔓延。"
        topWord in activate && avgEnergy < 2f -> "整体呈疲惫性亢奋状态，建议主动降速。"
        topWord in activate -> "激活状态偏多，保持觉察，避免过度消耗。"
        avgEnergy >= 4f -> "状态良好，神经系统处于平衡区间，继续保持。"
        else -> "状态在正常波动范围内。"
    }
    return "$freqDesc，共记录 $totalCount 条。$stateDesc"
}

// ── 全部记录视图 ──────────────────────────────────────────

@Composable
fun AllRecordsView(reviewDays: List<ReviewDay>) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        reviewDays.forEach { day ->
            item(key = day.dateLabel) {
                ReviewDayCard(day = day)
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
fun ReviewDayCard(day: ReviewDay) {
    var expanded by remember { mutableStateOf(true) }
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(day.dateLabel, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${day.records.size} 条  均能量 ${"%.1f".format(day.records.map { it.energyScore }.average())} 分",
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.45f)
                    )
                }
                Icon(if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    null, tint = MaterialTheme.colorScheme.onSurface.copy(0.4f))
            }

            AnimatedVisibility(visible = expanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 14.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.4f))
                    Spacer(Modifier.height(10.dp))
                    day.records.forEach { record ->
                        RecordItem(record = record)
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun RecordCard(record: HabitRecord) {
    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(record.timestamp))
    val color = stateWordColor(record.stateWord)
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.width(48.dp)) {
                Text(timeStr, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.4f))
                Text(record.anchorType.replace("锚点", ""), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.3f))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(6.dp), color = color.copy(0.12f)) {
                        Text(record.stateWord, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                            color = color, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    EnergyDots(record.energyScore)
                }
                if (!record.thought.isNullOrBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text("「${record.thought}」", fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.55f), lineHeight = 18.sp)
                }
            }
        }
    }
}

@Composable
fun RecordItem(record: HabitRecord) {
    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(record.timestamp))
    val color = stateWordColor(record.stateWord)
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.width(56.dp)) {
            Text(timeStr, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.4f))
            Text(record.anchorType.replace("锚点", ""), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.3f))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(6.dp), color = color.copy(0.12f)) {
                    Text(record.stateWord, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                        color = color, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
                Spacer(Modifier.width(8.dp))
                EnergyDots(record.energyScore)
            }
            if (!record.thought.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text("「${record.thought}」", fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.55f), lineHeight = 18.sp)
            }
        }
    }
}

@Composable
fun EnergyDots(score: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        repeat(5) { i ->
            Box(
                Modifier.size(8.dp).background(
                    if (i < score) MaterialTheme.colorScheme.primary.copy(0.75f)
                    else MaterialTheme.colorScheme.onSurface.copy(0.12f),
                    CircleShape
                )
            )
        }
    }
}
