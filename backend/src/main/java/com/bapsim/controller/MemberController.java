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
@RequestMapping("/api/members") // "/api/users" -> "/api/members"로 변경
public class MemberController { // UserController -> MemberController로 변경

    @Autowired
    private MemberRepository memberRepository; // UserRepository -> MemberRepository

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
    public ResponseEntity<Member> getMemberById(@PathVariable Long id) { // getUserById -> getMemberById
        Member member = memberRepository.findById(id).orElse(null); // User -> Member
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
    public ResponseEntity<Member> updateMember(@PathVariable Long id, @RequestBody Member memberDetails) { // updateUser -> updateMember, User -> Member
        Member member = memberRepository.findById(id).orElse(null); // User -> Member
        if (member != null) {
            // User의 getName(), getEmail()을 Member의 필드에 맞게 변경
            member.setUserName(memberDetails.getUserName());
            member.setUserEmail(memberDetails.getUserEmail());
            Member updatedMember = memberRepository.save(member); // updatedUser -> updatedMember
            return ResponseEntity.ok(updatedMember);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMember(@PathVariable Long id) { // deleteUser -> deleteMember
        Member member = memberRepository.findById(id).orElse(null); // User -> Member
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