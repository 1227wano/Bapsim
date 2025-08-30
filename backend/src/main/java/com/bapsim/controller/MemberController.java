package com.bapsim.controller;

import com.bapsim.dto.LoginRequestDto;
import com.bapsim.dto.LoginResponseDto;
import com.bapsim.entity.Member;
import com.bapsim.repository.MemberRepository;
import com.bapsim.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberService memberService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        Optional<Member> memberOptional = memberService.login(loginRequestDto.getUserId(), loginRequestDto.getUserPass());

        if (memberOptional.isPresent()) {
            Member member = memberOptional.get();
            LoginResponseDto responseDto = new LoginResponseDto(member.getUserName());
            return ResponseEntity.ok(responseDto);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping
    public List<Member> getAllMembers() { // getAllUsers -> getAllMembers
        return memberRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Member> getMemberById(@PathVariable Long id) {
        Member member = memberRepository.findById(id).orElse(null);
        if (member != null) {
            return ResponseEntity.ok(member);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public Member createMember(@RequestBody Member member) { // createUser -> createMember, User -> Member
        return memberRepository.save(member);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Member> updateMember(@PathVariable Long id, @RequestBody Member memberDetails) {
        Member member = memberRepository.findById(id).orElse(null);
        if (member != null) {
            member.setUserName(memberDetails.getUserName());
            member.setUserEmail(memberDetails.getUserEmail());
            Member updatedMember = memberRepository.save(member);
            return ResponseEntity.ok(updatedMember);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMember(@PathVariable Long id) {
        Member member = memberRepository.findById(id).orElse(null);
        if (member != null) {
            memberRepository.delete(member);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/test")
    public String test() {
        return "Hello from Bapsim Spring Boot Application!";
    }
}