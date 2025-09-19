# 배포 가이드

## 🚀 Heroku 배포 (추천 - 가장 간단)

### 1. Heroku CLI 설치
```bash
# macOS
brew install heroku/brew/heroku

# Windows
# https://devcenter.heroku.com/articles/heroku-cli 다운로드

# Linux
curl https://cli-assets.heroku.com/install.sh | sh
```

### 2. Heroku 로그인 및 앱 생성
```bash
# Heroku 로그인
heroku login

# 새 앱 생성
heroku create kakaobank-sheet-sync

# 환경 변수 설정
heroku config:set GOOGLE_SHEET_ID="your-spreadsheet-id"
heroku config:set GOOGLE_SHEET_NAME="입출금내역"
```

### 3. 배포
```bash
# Git 초기화 (아직 안했다면)
git init
git add .
git commit -m "Initial commit"

# Heroku에 배포
git push heroku main
```

### 4. 앱 확인
```bash
# 앱 URL 확인
heroku open

# 로그 확인
heroku logs --tail
```

## 🐳 Docker 배포

### 1. Docker 이미지 빌드
```bash
# JAR 파일 빌드
./mvnw clean package

# Docker 이미지 빌드
docker build -t kakaobank-sheet-sync .
```

### 2. Docker 컨테이너 실행
```bash
# 환경 변수 설정
export GOOGLE_SHEET_ID="your-spreadsheet-id"
export GOOGLE_SHEET_NAME="입출금내역"

# Docker Compose로 실행
docker-compose up -d
```

### 3. 확인
```bash
# 컨테이너 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs -f
```

## ☁️ AWS EC2 배포

### 1. EC2 인스턴스 생성
- 인스턴스 타입: t2.micro (무료 티어)
- OS: Ubuntu 20.04 LTS
- 보안 그룹: HTTP(80), HTTPS(443), SSH(22), Custom(8080)

### 2. 서버 설정
```bash
# Java 11 설치
sudo apt update
sudo apt install openjdk-11-jdk

# 애플리케이션 디렉토리 생성
mkdir -p /opt/kakaobank-sheet-sync
cd /opt/kakaobank-sheet-sync

# JAR 파일 업로드 (SCP 사용)
scp -i your-key.pem target/kakaobank-sheet-sync-1.0.0.jar ubuntu@your-ec2-ip:/opt/kakaobank-sheet-sync/

# credentials.json 업로드
scp -i your-key.pem src/main/resources/credentials.json ubuntu@your-ec2-ip:/opt/kakaobank-sheet-sync/
```

### 3. 서비스 등록 (systemd)
```bash
# 서비스 파일 생성
sudo nano /etc/systemd/system/kakaobank-sheet-sync.service
```

```ini
[Unit]
Description=KakaoBank Sheet Sync Service
After=network.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/opt/kakaobank-sheet-sync
ExecStart=/usr/bin/java -jar kakaobank-sheet-sync-1.0.0.jar
Restart=always
RestartSec=10
Environment=GOOGLE_SHEET_ID=your-spreadsheet-id
Environment=GOOGLE_SHEET_NAME=입출금내역

[Install]
WantedBy=multi-user.target
```

```bash
# 서비스 시작
sudo systemctl daemon-reload
sudo systemctl enable kakaobank-sheet-sync
sudo systemctl start kakaobank-sheet-sync

# 상태 확인
sudo systemctl status kakaobank-sheet-sync
```

## 📱 Android 앱 배포

### 1. Android Studio 프로젝트 생성
- 새 프로젝트 생성
- 백엔드 코드를 Android 서비스로 변환

### 2. 권한 설정
- NotificationListenerService 권한
- 인터넷 권한
- 백그라운드 실행 권한

### 3. APK 빌드 및 배포
```bash
# Debug APK 빌드
./gradlew assembleDebug

# Release APK 빌드
./gradlew assembleRelease
```

## 🔧 배포 전 체크리스트

- [ ] Google Cloud Console에서 서비스 계정 생성
- [ ] 구글 시트 생성 및 공유 설정
- [ ] credentials.json 파일 준비
- [ ] 환경 변수 설정
- [ ] 로컬 테스트 완료
- [ ] 도메인 설정 (선택사항)
- [ ] SSL 인증서 설정 (HTTPS 사용시)
- [ ] 모니터링 설정 (선택사항)

## 💰 비용 비교

| 방법 | 월 비용 | 설정 난이도 | 안정성 |
|------|---------|-------------|--------|
| Heroku | $0-7 | ⭐ | ⭐⭐⭐ |
| AWS EC2 | $0-10 | ⭐⭐ | ⭐⭐⭐⭐ |
| Docker | $0-20 | ⭐⭐⭐ | ⭐⭐⭐⭐ |
| Android | $0 | ⭐⭐⭐⭐ | ⭐⭐ |
