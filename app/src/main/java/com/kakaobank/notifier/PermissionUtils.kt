package com.kakaobank.notifier

import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat

object PermissionUtils {
    
    fun isNotificationAccessEnabled(context: Context): Boolean {
        return NotificationManagerCompat.getEnabledListenerPackages(context)
            .contains(context.packageName)
    }
    
    fun isNotificationPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= 33) {
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Android 12 이하에서는 자동으로 허용됨
        }
    }
    
    fun getPermissionStatus(context: Context): PermissionStatus {
        val notificationAccess = isNotificationAccessEnabled(context)
        val notificationPermission = isNotificationPermissionGranted(context)
        
        return when {
            notificationAccess && notificationPermission -> PermissionStatus.GRANTED
            !notificationAccess -> PermissionStatus.NEEDS_NOTIFICATION_ACCESS
            !notificationPermission -> PermissionStatus.NEEDS_NOTIFICATION_PERMISSION
            else -> PermissionStatus.DENIED
        }
    }
}

enum class PermissionStatus {
    GRANTED,
    NEEDS_NOTIFICATION_ACCESS,
    NEEDS_NOTIFICATION_PERMISSION,
    DENIED
}
