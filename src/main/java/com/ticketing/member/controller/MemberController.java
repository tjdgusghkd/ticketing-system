package com.ticketing.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ticketing.member.dto.MemberLoginRequest;
import com.ticketing.member.dto.MemberResponse;
import com.ticketing.member.dto.MemberSignupRequest;
import com.ticketing.member.service.MemberService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;
    
    @GetMapping("/signup")
    public String signupPage() {
    	return "member/signup";
    }
    
    @PostMapping("/signup")
    public Long signup(@RequestBody MemberSignupRequest request) {
        return memberService.signup(request);
    }
    
    @GetMapping("/signin")
    public String signinPage() {
    	return "member/signin";
    }

    @PostMapping("/login")
    public MemberResponse login(@RequestBody MemberLoginRequest request) {
        return memberService.login(request);
    }

    @GetMapping("/{id}")
    public MemberResponse find(@PathVariable Long id) {
        return memberService.findMember(id);
    }
}
