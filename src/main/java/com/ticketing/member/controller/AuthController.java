package com.ticketing.member.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticketing.member.dto.MemberLoginRequest;
import com.ticketing.member.dto.TokenReissueRequest;
import com.ticketing.member.dto.TokenResponse;
import com.ticketing.member.service.AuthService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody MemberLoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(@RequestBody TokenReissueRequest request) {
        return ResponseEntity.ok(authService.reissue(request));
    }
}