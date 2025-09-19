#!/bin/bash

# Google API 설정 테스트 스크립트

echo "=== 카카오뱅크 시트 동기화 앱 설정 테스트 ==="
echo ""

# 1. credentials.json 파일 확인
echo "1. Google 인증 파일 확인..."
if [ -f "src/main/resources/credentials.json" ]; then
    echo "✅ credentials.json 파일이 존재합니다."
    
    # JSON 파일 유효성 검사
    if python3 -m json.tool src/main/resources/credentials.json > /dev/null 2>&1; then
        echo "✅ JSON 파일 형식이 올바릅니다."
        
        # 필수 필드 확인
        if grep -q "client_email" src/main/resources/credentials.json && \
           grep -q "private_key" src/main/resources/credentials.json; then
            echo "✅ 필수 필드가 모두 존재합니다."
        else
            echo "❌ 필수 필드가 누락되었습니다. Google Cloud Console에서 서비스 계정을 다시 생성하세요."
            exit 1
        fi
    else
        echo "❌ JSON 파일 형식이 올바르지 않습니다."
        exit 1
    fi
else
    echo "❌ credentials.json 파일이 없습니다."
    echo "   Google Cloud Console에서 서비스 계정을 생성하고 JSON 키를 다운로드하세요."
    echo "   파일을 src/main/resources/credentials.json에 저장하세요."
    exit 1
fi

echo ""

# 2. 환경 변수 확인
echo "2. 환경 변수 확인..."
if [ -z "$GOOGLE_SHEET_ID" ]; then
    echo "⚠️  GOOGLE_SHEET_ID가 설정되지 않았습니다."
    echo "   export GOOGLE_SHEET_ID='your-spreadsheet-id' 명령으로 설정하세요."
else
    echo "✅ GOOGLE_SHEET_ID가 설정되었습니다: $GOOGLE_SHEET_ID"
fi

if [ -z "$GOOGLE_SHEET_NAME" ]; then
    echo "⚠️  GOOGLE_SHEET_NAME이 설정되지 않았습니다. 기본값 '입출금내역'을 사용합니다."
    export GOOGLE_SHEET_NAME="입출금내역"
else
    echo "✅ GOOGLE_SHEET_NAME이 설정되었습니다: $GOOGLE_SHEET_NAME"
fi

echo ""

# 3. Maven 빌드 테스트
echo "3. Maven 빌드 테스트..."
if ./mvnw clean compile -q; then
    echo "✅ Maven 빌드가 성공했습니다."
else
    echo "❌ Maven 빌드에 실패했습니다."
    exit 1
fi

echo ""

# 4. 애플리케이션 실행 테스트 (5초간)
echo "4. 애플리케이션 실행 테스트..."
echo "   애플리케이션을 5초간 실행하여 오류를 확인합니다..."

timeout 5s ./mvnw spring-boot:run -q 2>&1 | head -20

if [ $? -eq 124 ]; then
    echo "✅ 애플리케이션이 정상적으로 시작되었습니다."
elif [ $? -eq 0 ]; then
    echo "✅ 애플리케이션이 정상적으로 실행되었습니다."
else
    echo "❌ 애플리케이션 실행 중 오류가 발생했습니다."
    echo "   로그를 확인하여 문제를 해결하세요."
fi

echo ""
echo "=== 설정 테스트 완료 ==="
echo ""
echo "다음 단계:"
echo "1. Google Cloud Console에서 서비스 계정 생성"
echo "2. 구글 시트 생성 및 공유 설정"
echo "3. 환경 변수 설정"
echo "4. 애플리케이션 실행: ./mvnw spring-boot:run"
