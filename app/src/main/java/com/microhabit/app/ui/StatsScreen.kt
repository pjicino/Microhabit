package com.microhabit.app.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.microhabit.app.data.DailyStat
import com.microhabit.app.data.StateWordStat
import com.microhabit.app.viewmodel.HabitViewModel
import com.microhabit.app.viewmodel.StatsData

val suppressWords = setOf("木了", "空了", "软了", "沉了", "发懵")
val activateWords = setOf("紧绷", "焦灼", "亢奋", "烦躁")

fun stateWordColor(word: String): Color = when (word) {
    in suppressWords -> Color(0xFF78909C)
    in activateWords -> Color(0xFFEF5350)
    else -> Color(0xFF66BB6A)
}

@Composable
fun StatsScreen(vm: HabitViewModel, modifier: Modifier = Modifier) {
    val state by vm.uiState.collectAsState()
    val stats = state.stats

    if (stats.totalRecords == 0) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📭", fontSize = 48.sp)
                Spacer(Modifier.height(12.dp))
                Text("还没有记录", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
                Text("先去打个卡吧", fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
            }
        }
        return
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("统计", fontSize = 24.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SummaryCard("累计记录", "${stats.totalRecords} 次", Modifier.weight(1f))
            SummaryCard("平均能量", "%.1f 分".format(stats.avgEnergy), Modifier.weight(1f),
                highlight = stats.avgEnergy < 2f)
            SummaryCard("连续打卡", "${stats.streakDays} 天", Modifier.weight(1f),
                badge = if (stats.streakDays >= 7) "🔥" else null)
        }

        if (stats.dailyStats.isNotEmpty()) {
            SectionCard("近14天能量趋势") { EnergyLineChart(dailyStats = stats.dailyStats) }
        }

        if (stats.stateWordStats.isNotEmpty()) {
            SectionCard("状态词分布（近30天）") {
                val total = stats.stateWordStats.sumOf { it.count }.toFloat()
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    StateWordPieChart(stats = stats.stateWordStats, modifier = Modifier.size(140.dp))
                    Spacer(Modifier.width(16.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        stats.stateWordStats.take(6).forEach { item ->
                            val pct = if (total > 0) item.count / total * 100 else 0f
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(10.dp)
                                    .background(stateWordColor(item.stateWord), RoundedCornerShape(2.dp)))
                                Spacer(Modifier.width(6.dp))
                                Text(item.stateWord, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                Text("%.0f%%".format(pct), fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                            }
                        }
                    }
                }
            }
        }

        if (stats.stateWordStats.isNotEmpty()) {
            SectionCard("状态词频次") { StateWordBarChart(stats = stats.stateWordStats) }
        }

        if (stats.anchorFreq.isNotEmpty()) {
            SectionCard("锚点打卡频率") { AnchorFreqChart(freq = stats.anchorFreq) }
        }

        SectionCard("神经系统小结") { NervousSystemSummary(stats = stats) }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun SummaryCard(
    label: String, value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
    badge: String? = null
) {
    Card(
        modifier = modifier, shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            if (badge != null) Text(badge, fontSize = 16.sp)
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                color = if (highlight) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 11.sp, textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun EnergyLineChart(dailyStats: List<DailyStat>) {
    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(800, easing = EaseInOutCubic),
        label = "line"
    )
    val primary = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)

    Column {
        Canvas(modifier = Modifier.fillMaxWidth().height(130.dp)) {
            if (dailyStats.isEmpty()) return@Canvas
            val w = size.width; val h = size.height

            fun xOf(i: Int) = if (dailyStats.size == 1) w / 2f
                else i * w / (dailyStats.size - 1).toFloat()
            fun yOf(score: Float) = h - ((score - 1f) / 4f * h * 0.82f + h * 0.06f)

            for (lvl in 1..5) {
                val y = yOf(lvl.toFloat())
                drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 0.8.dp.toPx())
            }

            val fillPath = Path()
            dailyStats.forEachIndexed { i, s ->
                val x = xOf(i)
                val y = h - (h - yOf(s.avgScore)) * animProgress
                if (i == 0) fillPath.moveTo(x, y) else fillPath.lineTo(x, y)
            }
            fillPath.lineTo(xOf(dailyStats.lastIndex), h)
            fillPath.lineTo(xOf(0), h)
            fillPath.close()
            drawPath(fillPath, primary.copy(alpha = 0.08f))

            val linePath = Path()
            dailyStats.forEachIndexed { i, s ->
                val x = xOf(i)
                val y = h - (h - yOf(s.avgScore)) * animProgress
                if (i == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
            }
            drawPath(linePath, primary, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))

            dailyStats.forEachIndexed { i, s ->
                val x = xOf(i)
                val y = h - (h - yOf(s.avgScore)) * animProgress
                val dotColor = if (s.avgScore < 2f) errorColor else primary
                drawCircle(dotColor, 4.dp.toPx(), Offset(x, y))
                drawCircle(Color.White, 2.dp.toPx(), Offset(x, y))
            }
        }

        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            val step = if (dailyStats.size <= 7) 1 else dailyStats.size / 7
            dailyStats.filterIndexed { i, _ -> i % step == 0 }.forEach { day ->
                Text(day.date.takeLast(2) + "日", fontSize = 9.sp,
                    modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.35f))
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).background(primary.copy(0.8f), RoundedCornerShape(2.dp)))
            Text(" 正常", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
            Spacer(Modifier.width(12.dp))
            Box(Modifier.size(10.dp).background(errorColor.copy(0.8f), RoundedCornerShape(2.dp)))
            Text(" 低于2分", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
        }
    }
}

@Composable
fun StateWordPieChart(stats: List<StateWordStat>, modifier: Modifier = Modifier) {
    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(900, easing = EaseOutCubic),
        label = "pie"
    )
    val total = stats.sumOf { it.count }.toFloat()
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2, size.height / 2)
        var startAngle = -90f
        stats.forEach { item ->
            val sweep = (item.count / total) * 360f * animProgress
            drawArc(stateWordColor(item.stateWord), startAngle, sweep, useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2))
            drawArc(Color.White, startAngle, sweep, useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(2.dp.toPx()))
            startAngle += sweep
        }
        drawCircle(Color.White, radius * 0.45f, center)
    }
}

@Composable
fun StateWordBarChart(stats: List<StateWordStat>) {
    val maxCount = stats.maxOf { it.count }.toFloat()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        stats.forEach { item ->
            val targetRatio = if (maxCount > 0) item.count / maxCount else 0f
            val animRatio by animateFloatAsState(
                targetValue = targetRatio,
                animationSpec = tween(600),
                label = "bar_${item.stateWord}"
            )
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(item.stateWord, fontSize = 13.sp, modifier = Modifier.width(42.dp))
                Spacer(Modifier.width(8.dp))
                Box(Modifier.weight(1f).height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)) {
                    Box(Modifier.fillMaxHeight().fillMaxWidth(animRatio)
                        .clip(RoundedCornerShape(4.dp))
                        .background(stateWordColor(item.stateWord).copy(0.8f)))
                }
                Spacer(Modifier.width(8.dp))
                Text("${item.count}次", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.5f),
                    modifier = Modifier.width(32.dp))
            }
        }
    }
}

@Composable
fun AnchorFreqChart(freq: Map<String, Int>) {
    val total = freq.values.sum().toFloat()
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        freq.entries.toList().forEachIndexed { i, (anchor, count) ->
            val targetRatio = if (total > 0) count / total else 0f
            val animRatio by animateFloatAsState(
                targetValue = targetRatio,
                animationSpec = tween(600),
                label = "anchor_$i"
            )
            val color = colors.getOrElse(i) { colors[0] }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(anchor.replace("锚点", ""), fontSize = 13.sp)
                    Text("$count 次  ${"%.0f".format(targetRatio * 100)}%",
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                }
                Box(Modifier.fillMaxWidth().height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)) {
                    Box(Modifier.fillMaxHeight().fillMaxWidth(animRatio)
                        .clip(RoundedCornerShape(5.dp))
                        .background(color.copy(0.8f)))
                }
            }
        }
    }
}

@Composable
fun NervousSystemSummary(stats: StatsData) {
    val topWord = stats.stateWordStats.firstOrNull()?.stateWord ?: ""
    val summary = when {
        stats.avgEnergy < 2f && topWord in suppressWords ->
            "⚠️ 近期整体能量偏低，抑制状态频繁出现。\n神经系统可能处于慢性耗竭中，建议优先保障睡眠，减少额外刺激。"
        stats.avgEnergy < 2f && topWord in activateWords ->
            "⚠️ 能量虽低，但激活感较强——可能是「疲惫性亢奋」。\n看似活跃，实则消耗。建议主动降速，练习深呼吸。"
        stats.avgEnergy >= 4f ->
            "✅ 近期能量状态良好，神经系统处于相对平衡区间。\n继续保持当前节奏，注意不要过度消耗。"
        topWord in suppressWords ->
            "🔶 抑制状态词出现频率较高。整体能量尚可，但需留意「空转」信号，避免麻木蔓延。"
        topWord in activateWords ->
            "🔶 激活状态词较多，保持觉察即可。若持续出现「焦灼」，建议有意识安排放松时间。"
        else ->
            "🌿 整体状态在正常波动范围内。继续保持记录，帮助你看见自己的神经系统节律。"
    }
    Text(summary, fontSize = 14.sp, lineHeight = 22.sp,
        color = MaterialTheme.colorScheme.onSurface.copy(0.8f))
}
