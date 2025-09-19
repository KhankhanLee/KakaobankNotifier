package com.kakaobank.sheetsync.controller;

import com.kakaobank.sheetsync.model.Transaction;
import com.kakaobank.sheetsync.service.NotificationParserService;
import com.kakaobank.sheetsync.service.GoogleSheetsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 카카오뱅크 푸시 알림을 수신하고 처리하는 컨트롤러
 */
@RestController
@RequestMapping("/api")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationParserService notificationParserService;

    @Autowired
    private GoogleSheetsService googleSheetsService;

    /**
     * 카카오뱅크 푸시 알림을 수신하는 웹훅 엔드포인트
     * 실제 구현에서는 카카오뱅크 앱의 푸시 알림을 가로채는 방식이 필요합니다.
     */
    @PostMapping("/notification")
    public ResponseEntity<String> receiveNotification(@RequestBody Map<String, Object> notification) {
        try {
            logger.info("푸시 알림 수신: {}", notification);
            
            // 알림에서 거래 정보 파싱
            Transaction transaction = notificationParserService.parseNotification(notification);
            
            if (transaction != null) {
                // 구글 시트에 거래 내역 저장
                googleSheetsService.addTransaction(transaction);
                
                logger.info("거래 내역이 성공적으로 처리되었습니다: {}", transaction);
                return ResponseEntity.ok("거래 내역이 성공적으로 처리되었습니다.");
            } else {
                logger.warn("거래 정보를 파싱할 수 없습니다: {}", notification);
                return ResponseEntity.badRequest().body("유효하지 않은 거래 정보입니다.");
            }
            
        } catch (Exception e) {
            logger.error("알림 처리 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("알림 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 테스트용 엔드포인트 - 수동으로 거래 내역 추가
     */
    @PostMapping("/test/transaction")
    public ResponseEntity<String> addTestTransaction(@RequestBody Transaction transaction) {
        try {
            googleSheetsService.addTransaction(transaction);
            logger.info("테스트 거래 내역이 추가되었습니다: {}", transaction);
            return ResponseEntity.ok("테스트 거래 내역이 성공적으로 추가되었습니다.");
        } catch (Exception e) {
            logger.error("테스트 거래 내역 추가 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("테스트 거래 내역 추가 중 오류가 발생했습니다.");
        }
    }

    /**
     * 구글 시트의 모든 데이터를 조회하는 엔드포인트
     */
    @GetMapping("/sheet/data")
    public ResponseEntity<?> getSheetData() {
        try {
            var data = googleSheetsService.readAllData();
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            logger.error("시트 데이터 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("시트 데이터 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 헬스 체크 엔드포인트
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("서비스가 정상적으로 실행 중입니다.");
    }
}
