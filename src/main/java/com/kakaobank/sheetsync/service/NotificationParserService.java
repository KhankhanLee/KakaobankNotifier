package com.kakaobank.sheetsync.service;

import com.kakaobank.sheetsync.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 카카오뱅크 푸시 알림을 파싱하여 거래 정보를 추출하는 서비스
 */
@Service
public class NotificationParserService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationParserService.class);

    // 카카오뱅크 알림 패턴들
    private static final Pattern DEPOSIT_PATTERN = Pattern.compile(
            ".*입금\\s*([0-9,]+)원.*잔액\\s*([0-9,]+)원.*"
    );
    
    private static final Pattern WITHDRAWAL_PATTERN = Pattern.compile(
            ".*출금\\s*([0-9,]+)원.*잔액\\s*([0-9,]+)원.*"
    );
    
    private static final Pattern TRANSFER_PATTERN = Pattern.compile(
            ".*이체\\s*([0-9,]+)원.*잔액\\s*([0-9,]+)원.*"
    );

    private static final Pattern CARD_PAYMENT_PATTERN = Pattern.compile(
            ".*카드결제\\s*([0-9,]+)원.*잔액\\s*([0-9,]+)원.*"
    );

    /**
     * 푸시 알림 데이터를 파싱하여 Transaction 객체로 변환합니다.
     * 
     * @param notification 푸시 알림 데이터
     * @return 파싱된 거래 정보, 파싱 실패시 null
     */
    public Transaction parseNotification(Map<String, Object> notification) {
        try {
            // 알림 제목과 내용 추출
            String title = getStringValue(notification, "title", "");
            String body = getStringValue(notification, "body", "");
            String message = title + " " + body;

            logger.debug("알림 메시지 파싱 시도: {}", message);

            // 현재 시간 정보
            LocalDateTime now = LocalDateTime.now();
            String date = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String time = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

            // 각 패턴에 대해 매칭 시도
            Transaction transaction = tryParseDeposit(message, date, time);
            if (transaction != null) return transaction;

            transaction = tryParseWithdrawal(message, date, time);
            if (transaction != null) return transaction;

            transaction = tryParseTransfer(message, date, time);
            if (transaction != null) return transaction;

            transaction = tryParseCardPayment(message, date, time);
            if (transaction != null) return transaction;

            // 일반적인 금액 패턴으로 시도
            transaction = tryParseGenericAmount(message, date, time);
            if (transaction != null) return transaction;

            logger.warn("알림을 파싱할 수 없습니다: {}", message);
            return null;

        } catch (Exception e) {
            logger.error("알림 파싱 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 입금 알림 파싱
     */
    private Transaction tryParseDeposit(String message, String date, String time) {
        Matcher matcher = DEPOSIT_PATTERN.matcher(message);
        if (matcher.matches()) {
            String amount = matcher.group(1);
            String balance = matcher.group(2);
            return new Transaction(date, time, "입금", amount, balance, "입금");
        }
        return null;
    }

    /**
     * 출금 알림 파싱
     */
    private Transaction tryParseWithdrawal(String message, String date, String time) {
        Matcher matcher = WITHDRAWAL_PATTERN.matcher(message);
        if (matcher.matches()) {
            String amount = matcher.group(1);
            String balance = matcher.group(2);
            return new Transaction(date, time, "출금", amount, balance, "출금");
        }
        return null;
    }

    /**
     * 이체 알림 파싱
     */
    private Transaction tryParseTransfer(String message, String date, String time) {
        Matcher matcher = TRANSFER_PATTERN.matcher(message);
        if (matcher.matches()) {
            String amount = matcher.group(1);
            String balance = matcher.group(2);
            return new Transaction(date, time, "이체", amount, balance, "이체");
        }
        return null;
    }

    /**
     * 카드결제 알림 파싱
     */
    private Transaction tryParseCardPayment(String message, String date, String time) {
        Matcher matcher = CARD_PAYMENT_PATTERN.matcher(message);
        if (matcher.matches()) {
            String amount = matcher.group(1);
            String balance = matcher.group(2);
            return new Transaction(date, time, "카드결제", amount, balance, "카드결제");
        }
        return null;
    }

    /**
     * 일반적인 금액 패턴으로 파싱 시도
     */
    private Transaction tryParseGenericAmount(String message, String date, String time) {
        // 금액과 잔액이 포함된 패턴 찾기
        Pattern genericPattern = Pattern.compile(".*([0-9,]+)원.*잔액\\s*([0-9,]+)원.*");
        Matcher matcher = genericPattern.matcher(message);
        
        if (matcher.matches()) {
            String amount = matcher.group(1);
            String balance = matcher.group(2);
            
            // 거래 유형 추정
            String type = "기타";
            if (message.contains("입금")) type = "입금";
            else if (message.contains("출금")) type = "출금";
            else if (message.contains("이체")) type = "이체";
            else if (message.contains("카드")) type = "카드결제";
            
            return new Transaction(date, time, type, amount, balance, message);
        }
        
        return null;
    }

    /**
     * Map에서 문자열 값을 안전하게 추출
     */
    private String getStringValue(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    /**
     * 테스트용 메서드 - 샘플 알림 데이터 생성
     */
    public Map<String, Object> createSampleNotification(String type, String amount, String balance) {
        String title = "카카오뱅크";
        String body = String.format("%s %s원 잔액 %s원", type, amount, balance);
        
        return Map.of(
            "title", title,
            "body", body,
            "timestamp", System.currentTimeMillis()
        );
    }
}
