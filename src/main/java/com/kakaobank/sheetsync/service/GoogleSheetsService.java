package com.kakaobank.sheetsync.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import com.kakaobank.sheetsync.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

/**
 * Google Sheets API를 사용하여 데이터를 읽고 쓰는 서비스
 */
@Service
public class GoogleSheetsService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleSheetsService.class);
    private static final String APPLICATION_NAME = "KakaoBank Sheet Sync";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    @Value("${google.sheets.spreadsheet-id}")
    private String spreadsheetId;

    @Value("${google.sheets.sheet-name}")
    private String sheetName;

    @Value("${GOOGLE_CREDENTIALS_JSON:}")
    private String googleCredentialsJson;

    private Sheets service;

    @PostConstruct
    public void initialize() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        this.service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        
        logger.info("Google Sheets 서비스가 초기화되었습니다. 스프레드시트 ID: {}", spreadsheetId);
    }

    /**
     * Google API 인증을 위한 자격 증명을 생성합니다. (서비스 계정 방식)
     */
    private GoogleCredential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        InputStream in;
        
        // Railway 환경변수에서 Base64로 인코딩된 credentials 사용
        if (googleCredentialsJson != null && !googleCredentialsJson.isEmpty()) {
            logger.info("Railway 환경변수에서 Google 서비스 계정 키를 사용합니다.");
            byte[] decodedBytes = Base64.getDecoder().decode(googleCredentialsJson);
            in = new ByteArrayInputStream(decodedBytes);
        } else {
            // 로컬 개발환경에서는 credentials.json 파일 사용
            logger.info("로컬 credentials.json 파일을 사용합니다.");
            in = GoogleSheetsService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
            if (in == null) {
                throw new FileNotFoundException("자격 증명 파일을 찾을 수 없습니다: " + CREDENTIALS_FILE_PATH);
            }
        }
        
        // 서비스 계정 JSON 파일로부터 GoogleCredential 생성
        GoogleCredential credential = GoogleCredential.fromStream(in, HTTP_TRANSPORT, JSON_FACTORY)
                .createScoped(SCOPES);
        
        logger.info("Google 서비스 계정 인증이 완료되었습니다.");
        return credential;
    }

    /**
     * 거래 내역을 구글 시트에 추가합니다.
     */
    public void addTransaction(Transaction transaction) {
        try {
            // 헤더가 있는지 확인하고 없으면 추가
            ensureHeadersExist();
            
            // 새 행 추가 - 각 컬럼에 따로 넣기
            List<Object> rowData = Arrays.asList(
                    transaction.getDate(),     // A열: 날짜 
                    transaction.getTime(),     // B열: 시간 
                    transaction.getAmount(),         // C열: 금액
                    transaction.getType(),           // D열: 구분 (입금/출금 등)
                    transaction.getDescription()     // E열: 내용
            );
            
            ValueRange body = new ValueRange()
                    .setValues(Collections.singletonList(rowData));

            String range = sheetName + "!A:E";
            service.spreadsheets().values()
                    .append(spreadsheetId, range, body)
                    .setValueInputOption("USER_ENTERED")
                    .setInsertDataOption("INSERT_ROWS")
                    .execute();

            logger.info("거래 내역이 성공적으로 추가되었습니다: {} - {}", 
                       transaction.getType(), transaction.getAmount());
            
        } catch (Exception e) {
            logger.error("거래 내역 추가 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("구글 시트에 데이터를 추가하는 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 시트에 헤더가 있는지 확인하고 없으면 추가합니다.
     */
    private void ensureHeadersExist() {
        try {
            String range = sheetName + "!A1:F1";
            ValueRange response = service.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
            
            List<List<Object>> values = response.getValues();
            
            if (values == null || values.isEmpty() || values.get(0).isEmpty()) {
                // 헤더 추가 - 각 컬럼에 따로 넣기
                List<Object> headerData = Arrays.asList(
                        "날짜",     // A열
                        "시간",     // B열
                        "금액",     // C열
                        "구분",     // D열
                        "내용"      // E열
                );
                
                ValueRange headerBody = new ValueRange()
                        .setValues(Collections.singletonList(headerData));
                
                service.spreadsheets().values()
                        .update(spreadsheetId, range, headerBody)
                        .setValueInputOption("USER_ENTERED")
                        .execute();
                
                logger.info("구글 시트에 헤더가 추가되었습니다.");
            }
        } catch (Exception e) {
            logger.error("헤더 확인/추가 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 시트의 모든 데이터를 읽어옵니다.
     */
    public List<List<Object>> readAllData() {
        try {
            String range = sheetName + "!A:E";
            ValueRange response = service.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
            
            return response.getValues();
        } catch (Exception e) {
            logger.error("데이터 읽기 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("구글 시트에서 데이터를 읽는 중 오류가 발생했습니다.", e);
        }
    }
}
