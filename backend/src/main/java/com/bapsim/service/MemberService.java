package com.bapsim.service;

import com.bapsim.entity.Member;
import com.bapsim.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    /**
     * 로그인 로직
     * @param userId 사용자 아이디
     * @param userPass 사용자 비밀번호
     * @return 로그인 성공 시 Member 객체, 실패 시 Optional.empty()
     */
    public Optional<Member> login(String userId, String userPass) {
        // 1. userId로 회원 정보 조회
        Optional<Member> memberOptional = memberRepository.findByUserId(userId);

        // 2. 회원 정보가 존재하고 비밀번호가 일치하는지 확인
        if (memberOptional.isPresent()) {
            Member member = memberOptional.get();
            // 중요: 실제 프로덕션 환경에서는 반드시 BCrypt와 같은 해시 함수로 비밀번호를 비교해야 합니다.
            if (userPass.equals(member.getUserPass())) {
                return Optional.of(member); // 로그인 성공
            }
        }

        return Optional.empty(); // 로그인 실패 (사용자가 없거나 비밀번호가 틀림)
    }
}
