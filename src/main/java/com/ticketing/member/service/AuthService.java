package com.ticketing.member.service;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketing.global.jwt.JwtTokenProvider;
import com.ticketing.member.dto.MemberLoginRequest;
import com.ticketing.member.dto.TokenReissueRequest;
import com.ticketing.member.dto.TokenResponse;
import com.ticketing.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
	private static final String REFRESH_TOKEN_PREFIX = "RT:";

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider jwtTokenProvider;
	private final StringRedisTemplate stringRedisTemplate;

	public TokenResponse login(MemberLoginRequest request) {
		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getLoginId(), request.getPassword()));

		String loginId = authentication.getName();

		String accessToken = jwtTokenProvider.createAccessToken(loginId);
		String refreshToken = jwtTokenProvider.createRefreshToken(loginId);

		stringRedisTemplate.opsForValue().set(REFRESH_TOKEN_PREFIX + loginId, refreshToken,
				jwtTokenProvider.getRefreshTokenExpiration(), TimeUnit.MILLISECONDS);

		return new TokenResponse("Bearer", accessToken, refreshToken);
	}
	
	public TokenResponse reissue(TokenReissueRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 refresh token 입니다.");
        }

        if (!"refresh".equals(jwtTokenProvider.getTokenType(refreshToken))) {
            throw new IllegalArgumentException("refresh token 이 아닙니다.");
        }

        String loginId = jwtTokenProvider.getLoginId(refreshToken);
        String savedRefreshToken = stringRedisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + loginId);

        if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
            throw new IllegalArgumentException("저장된 refresh token 과 일치하지 않습니다.");
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(loginId);

        return new TokenResponse("Bearer", newAccessToken, refreshToken);
    }
	
	
}
