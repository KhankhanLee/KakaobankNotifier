package com.kakaobank.sheetsync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 카카오뱅크 입출금 내역을 구글 시트에 실시간으로 동기화하는 메인 애플리케이션
 */
@SpringBootApplication
public class KakaoBankSheetSyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(KakaoBankSheetSyncApplication.class, args);
    }
}
