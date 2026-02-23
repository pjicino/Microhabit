package com.microhabit.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.microhabit.app.viewmodel.HabitViewModel

val anchors = listOf("起床锚点", "下班锚点", "睡前锚点")

@Composable
fun MainScreen(vm: HabitViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            if (state.meltdown.isActive) {
                // ===== 熔断界面 =====
                MeltdownScreen(hoursRemaining = state.meltdown.hoursRemaining)
            } else {
                // ===== 正常打卡界面 =====
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(28.dp)
                ) {
                    // 顶部极简呼吸提示
                    Text(
                        text = "做一次深长呼气",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                        textAlign = TextAlign.Center,
                        letterSpacing = 2.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    // 三个锚点大按钮
                    anchors.forEach { anchor ->
                        AnchorButton(label = anchor) {
                            vm.openRecordSheet(anchor)
                        }
                    }
                }
            }
        }

        // 记录 BottomSheet
        if (state.showRecordSheet) {
            RecordBottomSheet(
                anchorType = state.currentAnchor,
                onDismiss = { vm.closeRecordSheet() },
                onSubmit = { stateWord, score, thought ->
                    vm.submitRecord(state.currentAnchor, stateWord, score, thought)
                },
                submitSuccess = state.submitSuccess,
                onSuccessDismiss = { vm.closeRecordSheet() }
            )
        }
    }
}

@Composable
fun MeltdownScreen(hoursRemaining: Long) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.padding(24.dp)
    ) {
        Text(
            text = "⛔",
            fontSize = 56.sp
        )
        Text(
            text = "系统已熔断",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        Text(
            text = "这2天请停止所有微习惯\n去睡觉。",
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            lineHeight = 34.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "还剩约 $hoursRemaining 小时自动解除",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    }
}

@Composable
fun AnchorButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = label,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp
        )
    }
}
