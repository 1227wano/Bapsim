package com.bapsim.service;

import com.bapsim.entity.*;
import com.bapsim.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class BasicDataService {
    
    private static final Logger logger = LoggerFactory.getLogger(BasicDataService.class);
    
    @Autowired
    private UniversityRepository universityRepository;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private CafeteriasRepository cafeteriasRepository;
    
    @Autowired
    private RestaurantsRepository restaurantsRepository;
    
    /**
     * 기본 데이터 초기화
     */
    @Transactional
    public void initializeBasicData() {
        logger.info("기본 데이터 초기화 시작");
        
        try {
            // 1. 대학교 데이터 생성
            University university = createUniversity();
            
            // 2. 사용자 데이터 생성
            Member member = createMember(university);
            
            // 3. 관리자 데이터 생성
            Member admin = createAdmin(university);
            
            // 4. 카페테리아 데이터 생성
            Cafeterias cafeteria = createCafeteria(university);
            
            // 5. 레스토랑 데이터 생성
            Restaurants restaurant = createRestaurant();
            
            logger.info("기본 데이터 초기화 완료");
            
        } catch (Exception e) {
            logger.error("기본 데이터 초기화 중 오류 발생", e);
        }
    }
    
    /**
     * SSAFY UNIVERSITY 생성
     */
    private University createUniversity() {
        // 이미 존재하는지 확인
        if (universityRepository.count() > 0) {
            return universityRepository.findAll().get(0);
        }
        
        University university = new University();
        university.setUniId(1);
        university.setUniName("SSAFY UNIVERSITY");
        
        University savedUniversity = universityRepository.save(university);
        logger.info("대학교 생성 완료: {}", savedUniversity.getUniName());
        
        return savedUniversity;
    }
    
    /**
     * 일반 사용자 김싸피 생성
     */
    private Member createMember(University university) {
        // 이미 존재하는지 확인
        if (memberRepository.findByUserId("1443254").isPresent()) {
            return memberRepository.findByUserId("1443254").get();
        }
        
        Member member = new Member();
        member.setUniId(university.getUniId());
        member.setUserId("1443254");
        member.setUserPass("123456");
        member.setUserName("김싸피");
        member.setUserEmail("1443254@ssafy.com");
        member.setUserPhone("010-1234-5678");
        member.setUserType("STUDENT");
        member.setUserStatus("ACTIVE");
        member.setCreatedId("system");
        member.setCreatedAt(LocalDateTime.now());
        member.setUpdatedId("system");
        member.setUpdatedAt(LocalDateTime.now());
        
        Member savedMember = memberRepository.save(member);
        logger.info("사용자 생성 완료: {} ({})", savedMember.getUserName(), savedMember.getUserId());
        
        return savedMember;
    }
    
    /**
     * 관리자 계정 생성
     */
    private Member createAdmin(University university) {
        // 이미 존재하는지 확인
        if (memberRepository.findByUserId("admin").isPresent()) {
            return memberRepository.findByUserId("admin").get();
        }
        
        Member admin = new Member();
        admin.setUniId(university.getUniId());
        admin.setUserId("admin");
        admin.setUserPass("admin123");
        admin.setUserName("시스템관리자");
        admin.setUserEmail("admin@ssafy.com");
        admin.setUserPhone("010-0000-0000");
        admin.setUserType("ADMIN");
        admin.setUserStatus("ACTIVE");
        admin.setCreatedId("system");
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedId("system");
        admin.setUpdatedAt(LocalDateTime.now());
        
        Member savedAdmin = memberRepository.save(admin);
        logger.info("관리자 생성 완료: {} ({})", savedAdmin.getUserName(), savedAdmin.getUserId());
        
        return savedAdmin;
    }
    
    /**
     * 카페테리아 생성
     */
    private Cafeterias createCafeteria(University university) {
        // 이미 존재하는지 확인
        if (cafeteriasRepository.count() > 0) {
            return cafeteriasRepository.findAll().get(0);
        }
        
        Cafeterias cafeteria = new Cafeterias();
        cafeteria.setUniId(university.getUniId());
        cafeteria.setBuildName("SSAFY 학생회관");
        cafeteria.setPhoneNo(212345678L);
        cafeteria.setOpenTime(LocalDateTime.of(2024, 1, 1, 7, 0, 0));
        cafeteria.setCloseTime(LocalDateTime.of(2024, 1, 1, 20, 0, 0));
        cafeteria.setRunYn("Y");
        cafeteria.setDelYn("N");
        cafeteria.setVisitor(0L);
        cafeteria.setCreatedId("system");
        cafeteria.setCreatedAt(LocalDateTime.now());
        cafeteria.setUpdatedId("system");
        cafeteria.setUpdatedAt(LocalDateTime.now());
        
        Cafeterias savedCafeteria = cafeteriasRepository.save(cafeteria);
        logger.info("카페테리아 생성 완료: {}", savedCafeteria.getBuildName());
        
        return savedCafeteria;
    }
    
    /**
     * 레스토랑 생성
     */
    private Restaurants createRestaurant() {
        // 이미 존재하는지 확인
        if (restaurantsRepository.count() > 0) {
            return restaurantsRepository.findAll().get(0);
        }
        
        Restaurants restaurant = new Restaurants();
        restaurant.setResName("SSAFY 레스토랑");
        restaurant.setAddress("서울시 강남구 테헤란로 123 SSAFY 빌딩");
        restaurant.setPhoneNo(298765432L);
        restaurant.setOpenTime(LocalDateTime.of(2024, 1, 1, 11, 0, 0));
        restaurant.setCloseTime(LocalDateTime.of(2024, 1, 1, 22, 0, 0));
        restaurant.setRunYn("Y");
        restaurant.setDelYn("N");
        restaurant.setVisitor(0L);
        restaurant.setCreatedId("system");
        restaurant.setCreatedAt(LocalDateTime.now());
        restaurant.setUpdatedId("system");
        restaurant.setUpdatedAt(LocalDateTime.now());
        
        Restaurants savedRestaurant = restaurantsRepository.save(restaurant);
        logger.info("레스토랑 생성 완료: {}", savedRestaurant.getResName());
        
        return savedRestaurant;
    }
}
