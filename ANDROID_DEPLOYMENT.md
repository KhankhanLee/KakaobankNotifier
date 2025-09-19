# Android 앱 배포 가이드

## 1. Android 프로젝트 생성

### 1.1 Android Studio에서 새 프로젝트 생성
- 프로젝트명: `KakaoBankSheetSync`
- 패키지명: `com.kakaobank.sheetsync`
- 최소 SDK: API 21 (Android 5.0)

### 1.2 의존성 추가 (build.gradle)
```gradle
dependencies {
    implementation 'com.google.apis:google-api-services-sheets:v4-rev612-1.25.0'
    implementation 'com.google.api-client:google-api-client:1.32.1'
    implementation 'com.google.http-client:google-http-client:1.43.3'
    implementation 'com.google.oauth-client:google-oauth-client-jetty:1.32.1'
    implementation 'com.fasterxml.jackson.core:jackson-databind:2.13.3'
    implementation 'org.apache.httpcomponents:httpclient:4.5.13'
}
```

## 2. 백그라운드 서비스 구현

### 2.1 NotificationListenerService 구현
```java
public class KakaoBankNotificationService extends NotificationListenerService {
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn.getPackageName().equals("com.kakaobank.android")) {
            // 알림 데이터 추출 및 처리
            processKakaoBankNotification(sbn);
        }
    }
}
```

### 2.2 권한 설정 (AndroidManifest.xml)
```xml
<uses-permission android:name="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<service
    android:name=".service.KakaoBankNotificationService"
    android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE">
    <intent-filter>
        <action android:name="android.service.notification.NotificationListenerService" />
    </intent-filter>
</service>
```

## 3. 사용자 인터페이스

### 3.1 메인 액티비티
- 설정 화면
- 로그 확인 화면
- 수동 테스트 기능

### 3.2 설정 화면
- Google 시트 ID 입력
- 시트 이름 설정
- 서비스 시작/중지

## 4. 배포

### 4.1 APK 빌드
```bash
./gradlew assembleRelease
```

### 4.2 Google Play Store 배포
- 개발자 계정 등록
- 앱 업로드
- 심사 대기

## 5. 주의사항

- NotificationListenerService 권한은 사용자가 수동으로 허용해야 함
- Google Play Store 정책 준수 필요
- 카카오뱅크 앱과의 호환성 유지
- 개인정보 보호 정책 준수
