package com.kakaobank.sheetsync;

import com.kakaobank.sheetsync.model.Transaction;
import com.kakaobank.sheetsync.service.NotificationParserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class NotificationParserServiceTest {

    private NotificationParserService notificationParserService;

    @BeforeEach
    void setUp() {
        notificationParserService = new NotificationParserService();
    }

    @Test
    void testParseDepositNotification() {
        // Given
        Map<String, Object> notification = Map.of(
            "title", "카카오뱅크",
            "body", "입금 100,000원 잔액 1,000,000원"
        );

        // When
        Transaction transaction = notificationParserService.parseNotification(notification);

        // Then
        assertNotNull(transaction);
        assertEquals("입금", transaction.getType());
        assertEquals("100,000", transaction.getAmount());
        assertEquals("1,000,000", transaction.getBalance());
    }

    @Test
    void testParseWithdrawalNotification() {
        // Given
        Map<String, Object> notification = Map.of(
            "title", "카카오뱅크",
            "body", "출금 50,000원 잔액 950,000원"
        );

        // When
        Transaction transaction = notificationParserService.parseNotification(notification);

        // Then
        assertNotNull(transaction);
        assertEquals("출금", transaction.getType());
        assertEquals("50,000", transaction.getAmount());
        assertEquals("950,000", transaction.getBalance());
    }

    @Test
    void testParseTransferNotification() {
        // Given
        Map<String, Object> notification = Map.of(
            "title", "카카오뱅크",
            "body", "이체 200,000원 잔액 750,000원"
        );

        // When
        Transaction transaction = notificationParserService.parseNotification(notification);

        // Then
        assertNotNull(transaction);
        assertEquals("이체", transaction.getType());
        assertEquals("200,000", transaction.getAmount());
        assertEquals("750,000", transaction.getBalance());
    }

    @Test
    void testParseCardPaymentNotification() {
        // Given
        Map<String, Object> notification = Map.of(
            "title", "카카오뱅크",
            "body", "카드결제 30,000원 잔액 720,000원"
        );

        // When
        Transaction transaction = notificationParserService.parseNotification(notification);

        // Then
        assertNotNull(transaction);
        assertEquals("카드결제", transaction.getType());
        assertEquals("30,000", transaction.getAmount());
        assertEquals("720,000", transaction.getBalance());
    }

    @Test
    void testParseInvalidNotification() {
        // Given
        Map<String, Object> notification = Map.of(
            "title", "카카오뱅크",
            "body", "일반 메시지입니다"
        );

        // When
        Transaction transaction = notificationParserService.parseNotification(notification);

        // Then
        assertNull(transaction);
    }

    @Test
    void testCreateSampleNotification() {
        // When
        Map<String, Object> sample = notificationParserService.createSampleNotification("입금", "100,000", "1,000,000");

        // Then
        assertNotNull(sample);
        assertEquals("카카오뱅크", sample.get("title"));
        assertTrue(sample.get("body").toString().contains("입금"));
        assertTrue(sample.get("body").toString().contains("100,000"));
        assertTrue(sample.get("body").toString().contains("1,000,000"));
    }
}
