package com.microhabit.app.ui

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.microhabit.app.viewmodel.HabitViewModel
import com.microhabit.app.viewmodel.StatsData

// ── 状态词分类（全局，ReviewScreen 也使用）─────────────────
val suppressWordSet: Set<String> = setOf("木了", "空了", "软了", "沉了", "发懵")
val activateWordSet: Set<String> = setOf("紧绷", "焦灼", "亢奋", "烦躁")

fun stateWordColor(word: String): Color {
    return when {
        suppressWordSet.contains(word) -> Color(0xFF78909C)
        activateWordSet.contains(word) -> Color(0xFFEF5350)
        else -> Color(0xFF66BB6A)
    }
}

@Composable
fun StatsScreen(vm: HabitViewModel, modifier: Modifier = Modifier) {
    val state by vm.uiState.collectAsState()
    val stats = state.stats

    if (stats.totalRecords == 0) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "📭", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "还没有记录",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
                Text(text = "先去打个卡吧", fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "统计", fontSize = 24.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp))

        // 概览三格
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SummaryCard(label = "累计记录", value = "${stats.totalRecords} 次",
                modifier = Modifier.weight(1f))
            SummaryCard(label = "平均能量", value = "%.1f 分".format(stats.avgEnergy),
                modifier = Modifier.weight(1f), highlight = stats.avgEnergy < 2f)
            SummaryCard(label = "连续打卡", value = "${stats.streakDays} 天",
                modifier = Modifier.weight(1f),
                badge = if (stats.streakDays >= 7) "🔥" else null)
        }

        // 折线图
        if (stats.dailyStats.isNotEmpty()) {
            SectionCard(title = "近14天能量趋势") {
                EnergyLineChart(stats = stats)
            }
        }

        // 饼图 + 图例
        if (stats.stateWordStats.isNotEmpty()) {
            SectionCard(title = "状态词分布（近30天）") {
                PieChartWithLegend(stats = stats)
            }
        }

        // 柱状图
        if (stats.stateWordStats.isNotEmpty()) {
            SectionCard(title = "状态词频次") {
                BarChart(stats = stats)
            }
        }

        // 锚点频率
        if (stats.anchorFreq.isNotEmpty()) {
            SectionCard(title = "锚点打卡频率") {
                AnchorChart(stats = stats)
            }
        }

        // 解读
        SectionCard(title = "神经系统小结") {
            NervousSystemSummary(stats = stats)
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun SummaryCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
    badge: String? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) MaterialTheme.colorScheme.errorContainer
                             else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (badge != null) {
                Text(text = badge, fontSize = 16.sp)
            }
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = if (highlight) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun EnergyLineChart(stats: StatsData) {
    val dailyStats = stats.dailyStats
    val animProgress: Float by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800, easing = EaseInOutCubic),
        label = "line"
    )
    val primary = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)

    Column {
        Canvas(modifier = Modifier.fillMaxWidth().height(130.dp)) {
            if (dailyStats.isEmpty()) return@Canvas
            val w = size.width
            val h = size.height
            val count = dailyStats.size

            fun xOf(i: Int): Float {
                return if (count == 1) w / 2f else i.toFloat() / (count - 1).toFloat() * w
            }
            fun yOf(score: Float): Float {
                return h - ((score - 1f) / 4f * h * 0.82f + h * 0.06f)
            }

            // 网格线
            for (lvl in 1..5) {
                val y = yOf(lvl.toFloat())
                drawLine(color = gridColor, start = Offset(0f, y),
                    end = Offset(w, y), strokeWidth = 0.8.dp.toPx())
            }

            // 填充
            val fillPath = Path()
            for (i in dailyStats.indices) {
                val item = dailyStats[i]
                val x = xOf(i)
                val y = h - (h - yOf(item.avgScore)) * animProgress
                if (i == 0) fillPath.moveTo(x, y) else fillPath.lineTo(x, y)
            }
            fillPath.lineTo(xOf(dailyStats.lastIndex), h)
            fillPath.lineTo(xOf(0), h)
            fillPath.close()
            drawPath(path = fillPath, color = primary.copy(alpha = 0.08f))

            // 折线
            val linePath = Path()
            for (i in dailyStats.indices) {
                val item = dailyStats[i]
                val x = xOf(i)
                val y = h - (h - yOf(item.avgScore)) * animProgress
                if (i == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
            }
            drawPath(path = linePath, color = primary,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))

            // 数据点
            for (i in dailyStats.indices) {
                val item = dailyStats[i]
                val x = xOf(i)
                val y = h - (h - yOf(item.avgScore)) * animProgress
                val dotColor = if (item.avgScore < 2f) errorColor else primary
                drawCircle(color = dotColor, radius = 4.dp.toPx(), center = Offset(x, y))
                drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(x, y))
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // X 轴标签
        val step = if (dailyStats.size <= 7) 1 else dailyStats.size / 7
        Row(modifier = Modifier.fillMaxWidth()) {
            for (i in dailyStats.indices) {
                if (i % step == 0) {
                    Text(
                        text = dailyStats[i].date.takeLast(2) + "日",
                        fontSize = 9.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(10.dp)
                .background(primary.copy(alpha = 0.8f), RoundedCornerShape(2.dp)))
            Text(text = " 正常", fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.size(10.dp)
                .background(errorColor.copy(alpha = 0.8f), RoundedCornerShape(2.dp)))
            Text(text = " 低于2分", fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun PieChartWithLegend(stats: StatsData) {
    val stateWordStats = stats.stateWordStats
    val total: Float = stateWordStats.sumOf { it.count }.toFloat()
    val animProgress: Float by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 900, easing = EaseOutCubic),
        label = "pie"
    )

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(140.dp)) {
            val radius = size.minDimension / 2f
            val cx = size.width / 2f
            val cy = size.height / 2f
            var startAngle = -90f

            for (item in stateWordStats) {
                val sweep = item.count.toFloat() / total * 360f * animProgress
                drawArc(
                    color = stateWordColor(item.stateWord),
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    topLeft = Offset(cx - radius, cy - radius),
                    size = Size(radius * 2f, radius * 2f)
                )
                drawArc(
                    color = Color.White,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    topLeft = Offset(cx - radius, cy - radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = 2.dp.toPx())
                )
                startAngle += sweep
            }
            drawCircle(color = Color.White, radius = radius * 0.45f, center = Offset(cx, cy))
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            for (item in stateWordStats.take(6)) {
                val pct = if (total > 0f) item.count.toFloat() / total * 100f else 0f
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp)
                        .background(stateWordColor(item.stateWord), RoundedCornerShape(2.dp)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = item.stateWord, fontSize = 12.sp,
                        modifier = Modifier.weight(1f))
                    Text(text = "%.0f%%".format(pct), fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun BarChart(stats: StatsData) {
    val stateWordStats = stats.stateWordStats
    val maxCount: Float = stateWordStats.maxOf { it.count }.toFloat()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (item in stateWordStats) {
            val targetRatio = if (maxCount > 0f) item.count.toFloat() / maxCount else 0f
            val animRatio: Float by animateFloatAsState(
                targetValue = targetRatio,
                animationSpec = tween(durationMillis = 600),
                label = "bar"
            )
            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()) {
                Text(text = item.stateWord, fontSize = 13.sp,
                    modifier = Modifier.width(42.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier.weight(1f).height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier.fillMaxHeight()
                            .fillMaxWidth(fraction = animRatio)
                            .clip(RoundedCornerShape(4.dp))
                            .background(stateWordColor(item.stateWord).copy(alpha = 0.8f))
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "${item.count}次", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.width(32.dp))
            }
        }
    }
}

@Composable
fun AnchorChart(stats: StatsData) {
    val freq = stats.anchorFreq
    val total: Float = freq.values.sum().toFloat()
    val colorList = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary
    )
    val entries = freq.entries.toList()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        for (i in entries.indices) {
            val anchor = entries[i].key
            val count = entries[i].value
            val targetRatio = if (total > 0f) count.toFloat() / total else 0f
            val animRatio: Float by animateFloatAsState(
                targetValue = targetRatio,
                animationSpec = tween(durationMillis = 600),
                label = "anchor"
            )
            val color = if (i < colorList.size) colorList[i] else colorList[0]

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = anchor.replace("锚点", ""), fontSize = 13.sp)
                    Text(
                        text = "$count 次  ${"%.0f".format(targetRatio * 100)}%",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Box(
                    modifier = Modifier.fillMaxWidth().height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier.fillMaxHeight()
                            .fillMaxWidth(fraction = animRatio)
                            .clip(RoundedCornerShape(5.dp))
                            .background(color.copy(alpha = 0.8f))
                    )
                }
            }
        }
    }
}

@Composable
fun NervousSystemSummary(stats: StatsData) {
    val topWord = if (stats.stateWordStats.isNotEmpty()) stats.stateWordStats[0].stateWord else ""
    val avg = stats.avgEnergy

    val summary: String = when {
        avg < 2f && suppressWordSet.contains(topWord) ->
            "⚠️ 近期整体能量偏低，抑制状态频繁出现。\n神经系统可能处于慢性耗竭中，建议优先保障睡眠，减少额外刺激。"
        avg < 2f && activateWordSet.contains(topWord) ->
            "⚠️ 能量虽低，但激活感较强——可能是「疲惫性亢奋」。\n看似活跃，实则消耗。建议主动降速，练习深呼吸。"
        avg >= 4f ->
            "✅ 近期能量状态良好，神经系统处于相对平衡区间。\n继续保持当前节奏，注意不要过度消耗。"
        suppressWordSet.contains(topWord) ->
            "🔶 抑制状态词出现频率较高。整体能量尚可，但需留意「空转」信号，避免麻木蔓延。"
        activateWordSet.contains(topWord) ->
            "🔶 激活状态词较多，保持觉察即可。若持续出现「焦灼」，建议有意识安排放松时间。"
        else ->
            "🌿 整体状态在正常波动范围内。继续保持记录，帮助你看见自己的神经系统节律。"
    }

    Text(text = summary, fontSize = 14.sp, lineHeight = 22.sp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
}
