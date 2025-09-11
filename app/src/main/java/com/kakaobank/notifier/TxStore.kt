package com.kakaobank.notifier

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 단순 인메모리 트랜잭션 저장소.
 * 앱 생명주기 동안 유지되며, 최신 10000건만 보관합니다.
 */
object TxStore {
    private const val MAX_ITEMS: Int = 10000

    private val _transactions: MutableStateFlow<List<Transaction>> = MutableStateFlow(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    fun add(transaction: Transaction) {
        val current: List<Transaction> = _transactions.value
        val updated: List<Transaction> = listOf(transaction) + current
        _transactions.value = if (updated.size > MAX_ITEMS) updated.take(MAX_ITEMS) else updated
    }
}


