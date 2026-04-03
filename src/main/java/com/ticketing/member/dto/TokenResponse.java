package com.ticketing.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 로그인 응답용 토큰 DTO
@Getter
@AllArgsConstructor
public class TokenResponse {
	private String grantType;
	private String accessToken;
	private String refreshToken;
}
