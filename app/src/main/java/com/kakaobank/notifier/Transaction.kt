package com.kakaobank.notifier

data class Transaction(
    val date: String,
    val time: String,
    val type: String,  // "입금" 또는 "출금"
    val amount: String,
    val balance: String,
    val description: String
)