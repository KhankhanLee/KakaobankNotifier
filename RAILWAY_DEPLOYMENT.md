# 🚀 Railway 배포 가이드

## 📋 **배포 전 준비사항**

### 1. **Railway 계정 생성**
- [Railway.app](https://railway.app)에서 계정 생성
- GitHub 계정으로 로그인 권장

### 2. **Railway CLI 설치**
```bash
npm install -g @railway/cli
```

### 3. **Railway 로그인**
```bash
railway login
```

## 🚀 **배포 단계**

### **Step 1: 프로젝트 초기화**
```bash
railway init
```

### **Step 2: 환경변수 설정**
```bash
# Google Sheets 설정
railway variables set GOOGLE_SHEET_ID="120ZkJS-aqutc0vTi4_oZxqKHSOt7IUCx5AdQ2aXcGgc"
railway variables set GOOGLE_SHEET_NAME="Sheet1"
railway variables set SPRING_PROFILES_ACTIVE="production"

# Google 서비스 계정 키 (Base64 인코딩된 값)
railway variables set GOOGLE_CREDENTIALS_JSON="ewogICJ0eXBlIjogInNlcnZpY2VfYWNjb3VudCIsCiAgInByb2plY3RfaWQiOiAicHJvcGFuZS1wcmltYWN5LTQ3MTgwNS1qMSIsCiAgInByaXZhdGVfa2V5X2lkIjogImNmMGY4MTAxMjMxYWViZTg0ZWRlMWFhMjJjY2JlNGRkNGY4ZTFkMGQiLAogICJwcml2YXRlX2tleSI6ICItLS0tLUJFR0lOIFBSSVZBVEUgS0VZLS0tLS1cbk1JSUV2QUlCQURBTkJna3Foa2lHOXcwQkFRRUZBQVNDQktZd2dnU2lBZ0VBQW9JQkFRQzdSUjl4WWdHclN2alNcbjU2MmtiRjI4T280NDB4SHYxcUt1WFBWSm5zZFpoTGh3ekwyZXV2engydFlPMngvd3Y4S0g0M1BaRVc4TWplTjFcblY2YitESWl4U1lrWFU2SUQvTFNZNTVnbmo0NmNXNmNIenBvTFBaMDkzbmlrOWF1MHg0VWFtQWpJWnhBVGViWTNcbkprZU0zRTZydTJWejBqaU4vekNENkt6eHIvb3J3elRqUllpR2lEaS9FT2M3L2FoVjBSc3F4YkRxUU1ORFJXcFZcblJGVVlMOVR1NHFRbDJQSkxzNUhrTUhOVmJQMVVDMHRkZkhQdzJ1aGpSMGhXTmtnRy9wSVN3aGdlK3Y4WFZlcVRcbnk2U0RmTzhHNFQ2R3BRZzZIeC95NTI2U3VKNEcyMnpkS0FpMjNGMVBWcjQ4S2U5MzJGSG41cEttTDhpd0dIVHVcbkVrT1A4ODJIQWdNQkFBRUNnZ0VBQlM0N1dYS2hvekIydzdiZkNkNXBsV3gxUk1NcUN0MkFNSnludUhxby95UDVcbnlqMFB2ZFZrYS9obTFaQmdTTk8xT1BjazNFdHNMQWU4MUNFeG9IT09VSUU5UnFUc3BYbVhNV2V1d09GcXk0NkpcbmgyREN5QzdUSzhQOHlHdDNiWS9iY2NuazdFNVZsb1JXY3g0ZStwbVpuNHlXbU9hbTQ5NjhZOW5LOXdDc1JndWJcblRFSmlVZjR6V0ZDQXByTWVTckFtak1TYTh1SStEYjl2TnN4TjlGdEh3NnhObnBjdlBDV2d3bTJQYlNadHd6dGVcbmtFM092WjhFNnFiOTNTVHU5MHlscE1QQWhUekpmcktHRWZmYVNkZjNxQ2M2azd1NFNPK1RFNWsvOFB3TDZTNVpcbnE1eEV5R0s0SnNvRXdjUXJ3VVBpVktmdndWREVZQ1B6d2Vkb0dLK2VHUUtCZ1FEZWdBejdyRFBuT1ptZWdsNDBcbk5CNm5qV1pRM3ZjZmtMd0dGNS9qZ2hQVXA0Z3FDYTJTWktOb1k1V3crUHU5NFFNYU1WRkFrOTBrdC84Ykx3NEVcbjJ4MHRKLzEwejFBQ1BFcDF1aDdxUnJmZjhPUmVYYVg2cnhQSmFCRDhrd1RDZDdFb1h2YUZlQ09ORmdVUU9UcnBcblQ2Z1NLUWtzQm4zbHNDNFA1ZXNYaTRhTWZRS0JnUURYZHl6ang4SWdUMmd1Sm9YQmliTm9EREhPaVZxNzJDczNcbkJlOTFCZWlXR2VCdjBUZjNYYkZKakNOU2tkWmNjWm54SE4zUXZsQkt0SGtaeWY0Ry9IMjJuWGw2WFM0UHRWZFFcbm9nMUdFaGNyQnVVeEV4Rm9HajVQZFRBSVdLbHNacHBuTy9walljOTRtMG9lY0dBeDNqL1duVnkxR01YWEFyR0JcbkpzVlJrR1lWVXdLQmdHMHA4alRBUmk3YUNjN0ZNNmIxTTEwTmVQZVFqVHFGYmxxbTJLRnN2b0lGT0Q4M3hGc2dcbjJUTnNPSk05ZFhXeG9pNEZxRkhBdWkwN3dRUDdVQ3Q5dlpEY0gvUmhpbVd1WXZIandTczhTeGFxUDhFMHZTeURcbkhYZ2hnTVFLOWJqYnRLV1VBZXQyQ0NzK2RIbURPcTRoa05wb0c4SG41WGo5dVh1L1dGcVM4QTZaQW9HQVp1dlZcbmRJU3g5UThaTzVwSklCYXZyUTdPSjlNeWxUSUFDb3Q2UTVMcjgrOHVaeHN3T2FmMmpLUTFKemJraDBuNGQ5VUxcbkc3QWMyR0oyeWx2VFRQRUU5OXNtOE0xYnVFTXVEeVVwWkFRMkx1WmZTbDdLWE1qVVpNNGNnSkFoZGRkWS8rRUZcbmpMMW5CanhmWGtUZG5sU0VkL2E1RndSYlZJWkQxZVo0TUtyaUtHTUNnWUFjVEl3QVhxYnZ2RlRkcTJSUENTMDZcbk9SbUd5MjJIWDdPOElCRm1TWTVsWWpwcm9CbEJaTUZneVppZmJaYnJNby9mRmNlNEVPamJMRmwyQ2tMRTk0UkpcblZTdUpHOWRoZm1WNEx0MlQ3SHdDeEQ2Z1pDUEZYSnFtN1U3aVRyRmtoOFE5bWEvSDBmak1mNm5wVFV4U2ZpdEJcbm9PWWQ1UjBGN2lBbDdZNVkwRmxOQlE9PVxuLS0tLS1FTkQgUFJJVkFURSBLRVktLS0tLVxuIiwKICAiY2xpZW50X2VtYWlsIjogInNoZWV0LXdyaXRlckBwcm9wYW5lLXByaW1hY3ktNDcxODA1LWoxLmlhbS5nc2VydmljZWFjY291bnQuY29tIiwKICAiY2xpZW50X2lkIjogIjEwNDA4NDE4NTkyMjI2MjE3MTkyMiIsCiAgImF1dGhfdXJpIjogImh0dHBzOi8vYWNjb3VudHMuZ29vZ2xlLmNvbS9vL29hdXRoMi9hdXRoIiwKICAidG9rZW5fdXJpIjogImh0dHBzOi8vb2F1dGgyLmdvb2dsZWFwaXMuY29tL3Rva2VuIiwKICAiYXV0aF9wcm92aWRlcl94NTA5X2NlcnRfdXJsIjogImh0dHBzOi8vd3d3Lmdvb2dsZWFwaXMuY29tL29hdXRoMi92MS9jZXJ0cyIsCiAgImNsaWVudF94NTA5X2NlcnRfdXJsIjogImh0dHBzOi8vd3d3Lmdvb2dsZWFwaXMuY29tL3JvYm90L3YxL21ldGFkYXRhL3g1MDkvc2hlZXQtd3JpdGVyJTQwcHJvcGFuZS1wcmltYWN5LTQ3MTgwNS1qMS5pYW0uZ3NlcnZpY2VhY2NvdW50LmNvbSIsCiAgInVuaXZlcnNlX2RvbWFpbiI6ICJnb29nbGVhcGlzLmNvbSJ9"
```

### **Step 3: 배포 실행**
```bash
railway up
```

### **Step 4: 배포 확인**
```bash
# 로그 확인
railway logs

# 도메인 확인
railway domain

# 상태 확인
railway status
```

## 🔧 **배포 후 테스트**

### **1. 헬스 체크**
```bash
curl https://your-app-name.railway.app/api/health
```

### **2. 테스트 거래 추가**
```bash
curl -X POST https://your-app-name.railway.app/api/test/transaction \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-01-15",
    "time": "14:30:00",
    "type": "입금",
    "amount": "100,000",
    "balance": "1,000,000",
    "description": "Railway 테스트"
  }'
```

## 📱 **Android 앱 연동**

### **API 엔드포인트**
- **기본 URL**: `https://your-app-name.railway.app`
- **푸시 알림 수신**: `POST /api/notification`
- **테스트 거래**: `POST /api/test/transaction`
- **헬스 체크**: `GET /api/health`

### **Android 앱 설정**
```java
public class ApiClient {
    private static final String BASE_URL = "https://your-app-name.railway.app";
    
    // API 호출 메서드들
}
```

## 🚨 **문제 해결**

### **1. 배포 실패 시**
```bash
# 로그 확인
railway logs

# 환경변수 확인
railway variables

# 재배포
railway up --detach
```

### **2. Google Sheets 연결 오류**
- Google Cloud Console에서 서비스 계정 권한 확인
- 스프레드시트 공유 설정 확인
- 환경변수 `GOOGLE_CREDENTIALS_JSON` 확인

### **3. 메모리 부족 오류**
- Railway 대시보드에서 메모리 사용량 확인
- 필요시 더 큰 플랜으로 업그레이드

## 💰 **비용 정보**

- **무료 플랜**: 월 $5 크레딧
- **사용량**: 동아리 축제용으로 충분
- **추가 비용**: 없음 (무료 플랜 내에서 사용 가능)

## 🎯 **다음 단계**

1. **Railway 배포 완료**
2. **Android 앱 개발**
3. **통합 테스트**
4. **축제 운영 준비**

---

**축제에서 성공적인 운영을 위해 화이팅! 🎉**
