package com.kakaobank.notifier

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

class KakaoBankNotificationService : NotificationListenerService() {

    companion object {
        private const val TAG = "KakaoBankNotif"
        private const val KAKAOBANK_PACKAGE = "com.kakaobank.channel"
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        val n = sbn ?: return
        if (n.packageName != KAKAOBANK_PACKAGE) return

        val extras = n.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()

        val joined = listOfNotNull(title, bigText, text)
            .joinToString(" | ")
            .trim()
        if (joined.isBlank()) return

        Log.d(TAG, "알림 수신: $joined")
        handle(joined)
    }

    private fun handle(notificationText: String) {
        val tx = parse(notificationText) ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 서버에서 구글 시트로 기록되는 엔드포인트(/api/test/transaction)로 전송
                val res = ApiClient.apiService.sendTransaction(tx)
                if (res.isSuccessful) {
                    Log.d(TAG, "트랜잭션 전송 성공: ${res.code()}")
                } else {
                    Log.e(TAG, "트랜잭션 전송 실패: ${res.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "트랜잭션 전송 오류: ${e.message}", e)
            }
        }

        // UI 표시용 인메모리 스토어에 추가
        try {
            TxStore.add(tx)
        } catch (e: Exception) {
            Log.e(TAG, "TxStore 추가 실패: ${e.message}", e)
        }
    }

    private fun parse(text: String): Transaction? {
        // 1) 제목 기반: "입금 1원" / "출금 2원" 등에서 타입/금액 추출
        val titleAmount = Pattern.compile("(입금|출금)\\s*(\\d{1,3}(?:,\\d{3})*|\\d+)\\s*원")
        val titleMatcher = titleAmount.matcher(text)

        val nowDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val nowTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

        if (titleMatcher.find()) {
            val type = titleMatcher.group(1) ?: ""
            val amount = (titleMatcher.group(2) ?: "").replace(",", "")

            // 2) 본문 기반: "보낸이 -> 받는이" 형태 추출
            val arrowPattern = Pattern.compile("\\n?\\s*([^|>]+?)\\s*->\\s*([^|<]+?)\\s*(?:\\\\||$)")
            val arrowMatcher = arrowPattern.matcher(text)
            val description = if (arrowMatcher.find()) {
                val from = arrowMatcher.group(1)?.trim() ?: ""
                val to = arrowMatcher.group(2)?.trim() ?: ""
                "$from -> $to"
            } else {
                // 화살표가 없으면 전체 텍스트 일부를 설명으로 저장
                text.take(120)
            }

            return Transaction(
                date = nowDate,
                time = nowTime,
                type = type,
                amount = amount,
                balance = "",
                description = description
            )
        }

        // 3) 구 패턴(잔액 포함)도 시도하여 하위 호환
        val depositLegacy = Pattern.compile("입금\\s*(\\d{1,3}(?:,\\d{3})*)\\s*원.*?잔액[: ]\\s*(\\d{1,3}(?:,\\d{3})*)\\s*원.*?-\\s*(.+)")
        val withdrawLegacy = Pattern.compile("출금\\s*(\\d{1,3}(?:,\\d{3})*)\\s*원.*?잔액[: ]\\s*(\\d{1,3}(?:,\\d{3})*)\\s*원.*?-\\s*(.+)")

        depositLegacy.matcher(text).takeIf { it.find() }?.let {
            return Transaction(
                date = nowDate,
                time = nowTime,
                type = "입금",
                amount = (it.group(1) ?: "").replace(",", ""),
                balance = (it.group(2) ?: "").replace(",", ""),
                description = it.group(3) ?: ""
            )
        }
        withdrawLegacy.matcher(text).takeIf { it.find() }?.let {
            return Transaction(
                date = nowDate,
                time = nowTime,
                type = "출금",
                amount = (it.group(1) ?: "").replace(",", ""),
                balance = (it.group(2) ?: "").replace(",", ""),
                description = it.group(3) ?: ""
            )
        }

        Log.d(TAG, "패턴 불일치: $text")
        return null
    }
}