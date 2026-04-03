package com.ticketing.member.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketing.member.dto.MemberLoginRequest;
import com.ticketing.member.dto.MemberResponse;
import com.ticketing.member.dto.MemberSignupRequest;
import com.ticketing.member.entity.Member;
import com.ticketing.member.enums.MemberStatus;
import com.ticketing.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

	public boolean isIdAvailable(String loginId) {
		return !memberRepository.existsByLoginId(loginId);
	}
	
	@Transactional
	public void signup(MemberSignupRequest dto) {
		if(memberRepository.existsByLoginId(dto.getLoginId())) {
			throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
		}
		
		if(memberRepository.existsByEmail(dto.getEmail())) {
			throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
		}
		
		if(memberRepository.existsByPhone(dto.getPhone())) {
			throw new IllegalArgumentException("이미 존재하는 휴대폰 번호입니다.");
		}
		
		String rawPassword = dto.getPassword();
		
		String encodePassword = passwordEncoder.encode(rawPassword);
		
		Member member = Member.builder()
						.loginId(dto.getLoginId())
						.password(encodePassword)
						.email(dto.getEmail())
						.phone(dto.getPhone())
						.gender(dto.getGender())
						.status(MemberStatus.ACTIVE)
						.build();
		try {
			memberRepository.save(member);
		} catch(DataIntegrityViolationException e){
			throw new IllegalArgumentException("이미 사용중인 회원정보입니다.");
		}
		
	}
}