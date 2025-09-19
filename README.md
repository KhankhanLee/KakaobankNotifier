# 카카오뱅크 입출금 내역 구글 시트 동기화 앱

카카오뱅크의 푸시 알림을 활용하여 입출금 내역을 실시간으로 구글 시트에 저장하는 Java 애플리케이션입니다.

## 주요 기능

- 카카오뱅크 푸시 알림 수신 및 파싱
- 구글 시트에 거래 내역 자동 저장
- 실시간 데이터 동기화
- REST API를 통한 데이터 조회 및 테스트

## 기술 스택

- Java 11
- Spring Boot 2.7.0
- Google Sheets API v4
- Maven

## 설정 방법

### 1. Google Cloud Console 설정

1. [Google Cloud Console](https://console.cloud.google.com/)에 접속
2. 새 프로젝트 생성 또는 기존 프로젝트 선택
3. Google Sheets API 활성화
4. 서비스 계정 생성 및 JSON 키 파일 다운로드
5. 다운로드한 JSON 파일을 `src/main/resources/credentials.json`으로 저장

### 2. 구글 시트 설정

1. 구글 시트 생성
2. 서비스 계정 이메일을 시트에 공유 (편집 권한 부여)
3. 시트 ID를 `application.yml`에 설정

### 3. 환경 변수 설정

```bash
export GOOGLE_SHEET_ID="your-spreadsheet-id"
export GOOGLE_SHEET_NAME="입출금내역"
export GOOGLE_CREDENTIALS_PATH="credentials.json"
```

### 4. 애플리케이션 실행

```bash
# 의존성 설치
mvn clean install

# 애플리케이션 실행
mvn spring-boot:run
```

## API 엔드포인트

### 1. 푸시 알림 수신
```
POST /api/notification
Content-Type: application/json

{
  "title": "카카오뱅크",
  "body": "입금 100,000원 잔액 1,000,000원"
}
```

### 2. 테스트 거래 내역 추가
```
POST /api/test/transaction
Content-Type: application/json

{
  "date": "2024-01-15",
  "time": "14:30:00",
  "type": "입금",
  "amount": "100,000",
  "balance": "1,000,000",
  "description": "급여"
}
```

### 3. 시트 데이터 조회
```
GET /api/sheet/data
```

### 4. 헬스 체크
```
GET /api/health
```

## 푸시 알림 연동 방법

### Android 앱을 통한 연동

1. Android 앱에서 푸시 알림을 가로채는 서비스 구현
2. 알림 데이터를 JSON 형태로 변환
3. 본 애플리케이션의 `/api/notification` 엔드포인트로 전송

### 예시 Android 코드 (참고용)

```java
// NotificationListenerService 구현
public class KakaoBankNotificationService extends NotificationListenerService {
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn.getPackageName().equals("com.kakaobank.android")) {
            // 알림 데이터 추출 및 전송
            sendNotificationToServer(notificationData);
        }
    }
}
```

## 주의사항

- 이 애플리케이션은 교육 및 개인 사용 목적으로 제작되었습니다.
- 실제 사용 시에는 보안 및 개인정보 보호에 주의하세요.
- 카카오뱅크의 서비스 약관을 준수해야 합니다.
- 푸시 알림을 가로채는 것은 Android의 NotificationListenerService 권한이 필요합니다.

## 문제 해결

### Google Sheets API 오류
- 서비스 계정 권한 확인
- JSON 키 파일 경로 확인
- 시트 공유 설정 확인

### 알림 파싱 오류
- 알림 메시지 형식 확인
- 정규식 패턴 조정 필요시 `NotificationParserService` 수정

## 라이선스

이 프로젝트는 개인 사용 목적으로 제작되었습니다.
