package com.bapsim.util;

import com.bapsim.config.SsafyApiConfig;
import com.bapsim.dto.SsafyApiHeader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * SSAFY API 헤더 정보를 자동으로 생성하는 유틸리티
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SsafyApiHeaderGenerator {
    
    private final SsafyApiConfig ssafyApiConfig;
    
    // 한국 시간대 상수
    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
    
    /**
     * SSAFY API 헤더 생성
     * @param apiName API 이름
     * @return 생성된 헤더 객체
     */
    public SsafyApiHeader generateHeader(String apiName) {
        SsafyApiHeader header = new SsafyApiHeader();
        
        header.setApiName(apiName);
        header.setTransmissionDate(generateTransmissionDate());
        header.setTransmissionTime(generateTransmissionTime());
        header.setInstitutionCode(ssafyApiConfig.getHeaders().getInstitutionCode());
        header.setFintechAppNo(ssafyApiConfig.getHeaders().getFintechAppNo());
        header.setApiServiceCode(apiName);
        header.setInstitutionTransactionUniqueNo(generateUniqueTransactionNo());
        header.setApiKey(ssafyApiConfig.getHeaders().getApiKey());
        header.setUserKey(ssafyApiConfig.getHeaders().getUserKey());
        
        log.debug("생성된 SSAFY API 헤더: {}", header);
        return header;
    }
    
    /**
     * 전송 날짜 생성 (yyyyMMdd 형식) - 한국 시간 기준
     */
    private String generateTransmissionDate() {
        LocalDateTime koreaTime = LocalDateTime.now(KOREA_ZONE);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String date = koreaTime.format(formatter);
        log.debug("생성된 전송 날짜 (KST): {}", date);
        return date;
    }
    
    /**
     * 전송 시간 생성 (HHmmss 형식) - 한국 시간 기준
     */
    private String generateTransmissionTime() {
        LocalDateTime koreaTime = LocalDateTime.now(KOREA_ZONE);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmmss");
        String time = koreaTime.format(formatter);
        log.debug("생성된 전송 시간 (KST): {}", time);
        return time;
    }
    
    /**
     * 고유 거래 번호 생성 (yyyyMMddHHmmss + 6자리 난수) - 한국 시간 기준
     */
    private String generateUniqueTransactionNo() {
        LocalDateTime koreaTime = LocalDateTime.now(KOREA_ZONE);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = koreaTime.format(formatter);
        
        // 6자리 난수 생성
        Random random = new Random();
        String randomPart = String.format("%06d", random.nextInt(1000000));
        
        String uniqueNo = timestamp + randomPart;
        log.debug("생성된 고유 거래 번호 (KST): {}", uniqueNo);
        return uniqueNo;
    }
}
