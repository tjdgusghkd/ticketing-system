package com.ticketing.global.jwt;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ticketing.member.entity.Member;
import com.ticketing.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
	
	private final MemberRepository memberRepository;
	
	@Override
	public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
		Member member = memberRepository.findByLoginId(loginId)
						.orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 아이디입니다."));
		return new User(
					member.getLoginId(),
					member.getPassword(),
					List.of(new SimpleGrantedAuthority("ROLE_USER"))
			);
	}
	
}
