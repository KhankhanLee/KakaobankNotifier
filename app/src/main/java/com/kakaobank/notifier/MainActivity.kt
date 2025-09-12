package com.kakaobank.notifier

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // ====== 토마토레드 테마 (라이트) ======
            val tomato = Color(0xFFFF6F61)
            val tomatoPressed = Color(0xFFE35B4E)

            val colorScheme = lightColorScheme(
                primary = tomato,
                onPrimary = Color.White,
                primaryContainer = tomato.copy(alpha = 0.15f),
                onPrimaryContainer = tomatoPressed,
                background = Color(0xFFFAFAFA),
                surface = Color.White,
                onSurface = Color(0xFF212121),
                outline = Color(0xFFE0E0E0)
            )

            MaterialTheme(colorScheme = colorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun MainScreen() {
    val context = LocalContext.current
    val activity = context as Activity
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // 권한 상태 실시간 확인
    val permissionStatus by remember {
        derivedStateOf { PermissionUtils.getPermissionStatus(context) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 텍스트 로고 "BN" 뱃지
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "BN",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Text(
                            "BankNotifier",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            Spacer(Modifier.height(20.dp))

            // ====== 기능 카드 (2열) ======
            Text(
                "빠른 작업",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))

            val actions = remember {
                listOf(
                    QuickAction("🔔", "알림 접근", isEnabled = permissionStatus != PermissionStatus.GRANTED) {
                        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    },
                    QuickAction("📣", "알림 권한", isEnabled = permissionStatus != PermissionStatus.GRANTED) {
                        if (Build.VERSION.SDK_INT >= 33) {
                            ActivityCompat.requestPermissions(
                                activity,
                                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                                1001
                            )
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "Android 13 이상에서 필요한 권한입니다.",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 0.dp, max = 360.dp)
            ) {
                items(actions) { a ->
                    ActionCard(icon = a.icon, label = a.label, isEnabled = a.isEnabled, onClick = a.onClick)
                }
            }

            Spacer(Modifier.height(24.dp))

            // ====== 최근 거래 내역 ======
            Text(
                "💰 최근 거래 내역",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))

            // 권한 상태 표시
            PermissionStatusCard(permissionStatus = permissionStatus)
            
            Spacer(Modifier.height(16.dp))

            // 실시간 TxStore 구독하여 표시
            val txs by TxStore.transactions.collectAsState()

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (txs.isEmpty()) {
                    item {
                        EmptyStateCard(permissionStatus = permissionStatus)
                    }
                } else {
                    items(txs) { t ->
                        val amountText = if (t.amount.isNotBlank()) "₩${t.amount}" else "-"
                        val senderOrType = if (t.type.isNotBlank()) t.type else "거래"
                        val memo = t.description
                        val timeText = formatTime(t.time)
                        TxCard(
                            ui = TxUi(
                                amount = amountText,
                                sender = senderOrType,
                                memo = memo,
                                time = timeText,
                                type = t.type,
                            )
                        )
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

/* ------------------------------ UI 컴포넌트 ------------------------------ */

private data class QuickAction(
    val icon: String,
    val label: String,
    val isEnabled: Boolean = true,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionCard(
    icon: String,
    label: String,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    Card(
        onClick = if (isEnabled) onClick else { {} },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                icon, 
                fontWeight = FontWeight.SemiBold, 
                color = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
            if (!isEnabled) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "완료",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private data class TxUi(
    val amount: String,
    val sender: String,
    val memo: String,
    val time: String,
    val type: String = ""
)

@Composable
private fun TxCard(ui: TxUi) {
    val isDeposit = ui.type == "입금"
    val typeColor = if (isDeposit) Color(0xFF4CAF50) else Color(0xFFF44336)
    val typeIcon = if (isDeposit) "↗" else "↙"
    
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 거래 타입별 아이콘
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(typeColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    typeIcon,
                    style = MaterialTheme.typography.titleLarge,
                    color = typeColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    ui.amount, 
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = typeColor
                )
                Text(ui.sender, style = MaterialTheme.typography.bodyLarge, color = Color(0xFF444444))
                Text(ui.memo, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF666666))
            }

            Text(
                ui.time,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF999999),
                textAlign = TextAlign.End
            )
        }

        // 거래 타입별 색상 스트립
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(typeColor.copy(alpha = 0.9f))
        )
    }
}

@Composable
private fun PermissionStatusCard(permissionStatus: PermissionStatus) {
    val (statusText, statusColor, statusIcon) = when (permissionStatus) {
        PermissionStatus.GRANTED -> Triple("모든 권한이 허용되었습니다", Color(0xFF4CAF50), "✅")
        PermissionStatus.NEEDS_NOTIFICATION_ACCESS -> Triple("알림 접근 권한이 필요합니다", Color(0xFFFF9800), "⚠️")
        PermissionStatus.NEEDS_NOTIFICATION_PERMISSION -> Triple("알림 권한이 필요합니다", Color(0xFFFF9800), "⚠️")
        PermissionStatus.DENIED -> Triple("권한이 거부되었습니다", Color(0xFFF44336), "❌")
    }
    
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = statusColor.copy(alpha = 0.1f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                statusIcon,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                statusText,
                style = MaterialTheme.typography.bodyMedium,
                color = statusColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun EmptyStateCard(permissionStatus: PermissionStatus) {
    val (title, description) = when (permissionStatus) {
        PermissionStatus.GRANTED -> "거래 알림을 기다리는 중..." to "카카오뱅크에서 입출금 알림이 오면 자동으로 기록됩니다."
        else -> "권한을 허용해주세요" to "알림 접근 권한을 허용하면 거래 내역이 여기에 표시됩니다."
    }
    
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "💰",
                style = MaterialTheme.typography.displayLarge
            )
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatTime(timeString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("a h:mm", Locale.getDefault())
        val date = inputFormat.parse(timeString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        timeString
    }
}

