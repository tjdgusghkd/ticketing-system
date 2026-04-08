package com.ticketing.member.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticketing.member.dto.MemberLoginRequest;
import com.ticketing.member.dto.MyReservationResponseDto;
import com.ticketing.member.dto.TokenReissueRequest;
import com.ticketing.member.dto.TokenResponse;
import com.ticketing.member.service.AuthService;
import com.ticketing.member.service.MemberService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final MemberService memberService;
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody MemberLoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(@RequestBody TokenReissueRequest request) {
        return ResponseEntity.ok(authService.reissue(request));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<String> logout(Authentication authentication) {
    	authService.logout(authentication.getName());
    	return ResponseEntity.ok("로그아웃 완료");
    }
    
    @GetMapping("/members/me/reservations")
    public ResponseEntity<List<MyReservationResponseDto>> getMyReservation(Authentication authentication) {
    	
    	return ResponseEntity.ok(memberService.getMyReservations(authentication.getName()));
    }	
    
}