# Google API 설정 가이드

## 1. Google Cloud Console 설정

### 1.1 프로젝트 생성
1. [Google Cloud Console](https://console.cloud.google.com/)에 접속
2. 새 프로젝트 생성 또는 기존 프로젝트 선택
3. 프로젝트 이름: `kakaobank-sheet-sync` (또는 원하는 이름)

### 1.2 Google Sheets API 활성화
1. 왼쪽 메뉴에서 "API 및 서비스" > "라이브러리" 클릭
2. "Google Sheets API" 검색
3. "Google Sheets API" 클릭 후 "사용" 버튼 클릭

### 1.3 서비스 계정 생성
1. "API 및 서비스" > "사용자 인증 정보" 클릭
2. "사용자 인증 정보 만들기" > "서비스 계정" 선택
3. 서비스 계정 정보 입력:
   - 이름: `kakaobank-sheet-sync-service`
   - 설명: `카카오뱅크 시트 동기화용 서비스 계정`
4. "만들기 및 계속하기" 클릭
5. 역할: "편집자" 또는 "소유자" 선택
6. "완료" 클릭

### 1.4 JSON 키 파일 다운로드
1. 생성된 서비스 계정 클릭
2. "키" 탭 클릭
3. "키 추가" > "새 키 만들기" 선택
4. 키 유형: "JSON" 선택
5. "만들기" 클릭
6. 다운로드된 JSON 파일을 `src/main/resources/credentials.json`으로 저장

## 2. 구글 시트 설정

### 2.1 새 시트 생성
1. [Google Sheets](https://sheets.google.com/)에 접속
2. 새 스프레드시트 생성
3. 시트 이름을 "입출금내역"으로 변경

### 2.2 시트 공유 설정
1. 시트에서 "공유" 버튼 클릭
2. 서비스 계정 이메일 주소 추가 (JSON 파일의 `client_email` 값)
3. 권한: "편집자" 선택
4. "완료" 클릭

### 2.3 시트 ID 확인
1. 시트 URL에서 시트 ID 복사
   - URL 예시: `https://docs.google.com/spreadsheets/d/1ABC123.../edit`
   - 시트 ID: `1ABC123...` (슬래시 사이의 긴 문자열)

## 3. 애플리케이션 설정

### 3.1 credentials.json 파일 배치
```
src/main/resources/credentials.json
```

### 3.2 환경 변수 설정
```bash
export GOOGLE_SHEET_ID="your-spreadsheet-id-here"
export GOOGLE_SHEET_NAME="입출금내역"
```

### 3.3 application.yml 수정
```yaml
google:
  sheets:
    spreadsheet-id: ${GOOGLE_SHEET_ID:your-spreadsheet-id}
    sheet-name: ${GOOGLE_SHEET_NAME:입출금내역}
```

## 4. 테스트

### 4.1 애플리케이션 실행
```bash
./mvnw spring-boot:run
```

### 4.2 헬스 체크
```bash
curl http://localhost:8080/api/health
```

### 4.3 테스트 거래 추가
```bash
curl -X POST http://localhost:8080/api/test/transaction \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-01-15",
    "time": "14:30:00",
    "type": "입금",
    "amount": "100,000",
    "balance": "1,000,000",
    "description": "테스트 입금"
  }'
```

## 5. 보안 주의사항

- `credentials.json` 파일은 절대 Git에 커밋하지 마세요
- `.gitignore`에 `credentials.json` 추가
- 프로덕션에서는 환경 변수로 관리
- 서비스 계정 권한을 최소한으로 유지
