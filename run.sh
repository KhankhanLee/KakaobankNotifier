#!/bin/bash

# 카카오뱅크 시트 동기화 앱 실행 스크립트

echo "카카오뱅크 시트 동기화 앱을 시작합니다..."

# 환경 변수 설정 (필요시 수정)
export GOOGLE_SHEET_ID="your-spreadsheet-id"
export GOOGLE_SHEET_NAME="입출금내역"
export GOOGLE_CREDENTIALS_PATH="credentials.json"

# Maven을 사용하여 애플리케이션 실행
mvn spring-boot:run
