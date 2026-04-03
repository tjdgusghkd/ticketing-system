package com.ticketing.member.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ticketing.member.dto.MemberSignupRequest;
import com.ticketing.member.service.MemberService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;
    
    // 회원가입 페이지 매핑
    @GetMapping("/signup")
    public String signupPage() {
    	return "member/signup";
    }
    
    // 로그인 페이지 매핑
    @GetMapping("/signin")
    public String signinPage() {
    	return "member/signin";
    }
    
    // 아이디 중복확인 로직
    @GetMapping("/check-id")
    @ResponseBody
    public Map<String, Boolean> checkId(@RequestParam("loginId") String loginId){
    	boolean available = memberService.isIdAvailable(loginId);
    	
    	Map<String, Boolean> result = new HashMap<String, Boolean>();
    	result.put("available", available);
    	return result;
    }
    
    // 회원가입 로직
    @PostMapping("/signup")
    public String signup(@ModelAttribute MemberSignupRequest dto) {
    	memberService.signup(dto);
    	return "redirect:/";
    }
}
