#!/bin/bash

echo "🚀 Railway 배포 설정 시작..."

# Railway 프로젝트 초기화
echo "📦 Railway 프로젝트 초기화 중..."
railway init

# 환경변수 설정
echo "🔧 환경변수 설정 중..."
railway variables set GOOGLE_SHEET_ID="120ZkJS-aqutc0vTi4_oZxqKHSOt7IUCx5AdQ2aXcGgc"
railway variables set GOOGLE_SHEET_NAME="Sheet1"
railway variables set SPRING_PROFILES_ACTIVE="production"

# Google 서비스 계정 키 설정 (credentials.json 내용을 base64로 인코딩)
echo "🔑 Google 서비스 계정 키 설정 중..."
if [ -f "src/main/resources/credentials.json" ]; then
    # credentials.json을 base64로 인코딩하여 환경변수로 설정
    CREDENTIALS_BASE64=$(base64 -i src/main/resources/credentials.json)
    railway variables set GOOGLE_CREDENTIALS_JSON="$CREDENTIALS_BASE64"
    echo "✅ Google 서비스 계정 키가 설정되었습니다."
else
    echo "❌ credentials.json 파일을 찾을 수 없습니다."
    echo "   src/main/resources/credentials.json 파일을 생성해주세요."
    exit 1
fi

echo "🎉 Railway 설정이 완료되었습니다!"
echo "📋 다음 단계:"
echo "   1. railway up 명령어로 배포"
echo "   2. railway logs 명령어로 로그 확인"
echo "   3. railway domain 명령어로 도메인 확인"


