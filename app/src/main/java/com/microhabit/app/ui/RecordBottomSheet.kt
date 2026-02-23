package com.microhabit.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

val stateGroups = linkedMapOf(
    "偏激活 ↑" to listOf("紧绷", "焦灼", "亢奋", "烦躁"),
    "偏抑制 ↓" to listOf("木了", "空了", "软了", "沉了", "发懵"),
    "平衡 ○"  to listOf("轻盈", "平稳", "清醒", "松弛", "安静")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordBottomSheet(
    anchorType: String,
    onDismiss: () -> Unit,
    onSubmit: (stateWord: String, score: Int, thought: String?) -> Unit,
    submitSuccess: Boolean,
    onSuccessDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedWord by remember { mutableStateOf("") }
    var energyScore by remember { mutableFloatStateOf(3f) }
    var thought by remember { mutableStateOf("") }

    LaunchedEffect(submitSuccess) {
        if (submitSuccess) { delay(800); onSuccessDismiss() }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(anchorType, fontSize = 22.sp, fontWeight = FontWeight.Bold)

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("能量感", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text("${energyScore.roundToInt()} / 5", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                }
                Slider(value = energyScore, onValueChange = { energyScore = it }, valueRange = 1f..5f, steps = 3)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("耗尽", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f))
                    Text("满电", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f))
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("现在是什么感觉？（必选）", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                stateGroups.forEach { (groupName, words) ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(groupName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(words) { word ->
                                FilterChip(
                                    selected = selectedWord == word,
                                    onClick  = { selectedWord = word },
                                    label    = { Text(word, fontSize = 14.sp) },
                                    colors   = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor     = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            if (anchorType == "睡前锚点") {
                OutlinedTextField(
                    value = thought,
                    onValueChange = { thought = it },
                    placeholder = { Text("今天的一个念头", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
            }

            if (submitSuccess) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("✓ 已记录", fontSize = 18.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = { if (selectedWord.isNotEmpty()) onSubmit(selectedWord, energyScore.roundToInt(), thought.ifBlank { null }) },
                    enabled = selectedWord.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("记录下来", fontSize = 16.sp)
                }
            }
        }
    }
}
