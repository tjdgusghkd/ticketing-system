package com.ticketing.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 재발급 요청 DTO
@Getter
@AllArgsConstructor
public class TokenReissueRequest {
	private String refreshToken;
}
