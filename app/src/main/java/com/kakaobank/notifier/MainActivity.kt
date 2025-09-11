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
                    QuickAction("🔔", "알림 접근") {
                        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    },
                    QuickAction("📣", "알림 권한") {
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
                    },
                    QuickAction("🚀", "테스트 전송") {
                        scope.launch {
                            val nowDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                            val nowTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                            val tx = Transaction(
                                date = nowDate,
                                time = nowTime,
                                type = "입금",
                                amount = "100,000",
                                balance = "1,000,000",
                                description = "앱 테스트 전송"
                            )

                            Log.d("MainActivity", "전송할 데이터: $tx")

                            try {
                                val res = ApiClient.apiService.sendTransaction(tx)
                                Log.d("MainActivity", "응답 코드: ${res.code()}")

                                if (res.isSuccessful) {
                                    val responseBody = res.body() ?: "응답 없음"
                                    Log.d("MainActivity", "응답 본문: $responseBody")
                                    snackbarHostState.showSnackbar("전송 성공", duration = SnackbarDuration.Short)
                                } else {
                                    Log.e("MainActivity", "응답 실패: ${res.code()}")
                                    snackbarHostState.showSnackbar("전송 실패: ${res.code()}", duration = SnackbarDuration.Short)
                                }
                            } catch (e: Exception) {
                                Log.e("MainActivity", "전송 오류: ${e.message}", e)
                                snackbarHostState.showSnackbar("오류: ${e.message}", duration = SnackbarDuration.Short)
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
                    ActionCard(icon = a.icon, label = a.label, onClick = a.onClick)
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

            // 실시간 TxStore 구독하여 표시
            val txs by TxStore.transactions.collectAsState()

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (txs.isEmpty()) {
                    item {
                        Text(
                            text = "아직 거래 알림이 없습니다.",
                            color = Color(0xFF777777),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    items(txs) { t ->
                        val amountText = if (t.amount.isNotBlank()) "₩${t.amount}" else "-"
                        val senderOrType = if (t.type.isNotBlank()) t.type else "거래"
                        val memo = t.description
                        val timeText = t.time
                        TxCard(
                            ui = TxUi(
                                amount = amountText,
                                sender = senderOrType,
                                memo = memo,
                                time = timeText
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
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionCard(
    icon: String,
    label: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
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
            Text(icon, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

private data class TxUi(
    val amount: String,
    val sender: String,
    val memo: String,
    val time: String
)

@Composable
private fun TxCard(ui: TxUi) {
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
            // 아바타
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(Color(0xFFE0E0E0))
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(ui.amount, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
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

        // 좌측 포인트 스트립(토마토레드)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.9f))
        )
    }
}

