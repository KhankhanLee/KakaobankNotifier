package com.kakaobank.notifier

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/test/transaction")
    suspend fun sendTransaction(@Body transaction: Transaction): Response<String>

    @POST("api/notification")
    suspend fun sendNotification(@Body notification: Map<String, Any>): Response<String>
}