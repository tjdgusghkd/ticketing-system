package com.ticketing.global.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;


@Component
@Getter
public class JwtTokenProvider {
	
	@Value("${jwt.secret}")
	private String secret;
	
	@Value("${jwt.access-token-expiration}")
	private long accessTokenExpiration;
	
	@Value("${jwt.refresh-token-expiration}")
	private long refreshTokenExpiration;
	
	private SecretKey secretKey;
	
	@PostConstruct
	public void init() {
		this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}
	
	public String createAccessToken(String loginId) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + accessTokenExpiration);
		
		return Jwts.builder()
				.subject(loginId)
				.claim("type","access")
				.issuedAt(now)
				.expiration(expiry)
				.signWith(secretKey)
				.compact();
	}
	
	public String createRefreshToken(String loginId) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + refreshTokenExpiration);
		
		return Jwts.builder()
				.subject(loginId)
				.claim("type", "refresh")
				.issuedAt(now)
				.expiration(expiry)
				.signWith(secretKey)
				.compact();
	}
	
	public boolean validateToken(String token) {
		try {
			Jwts.parser()
						.verifyWith(secretKey)
						.build()
						.parseSignedClaims(token);
			return true;
		} catch(JwtException | IllegalArgumentException e) {
			return false;
		}
	}
	
	public String getLoginId(String token) {
		return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload()
				.getSubject();
	}
	
	public String getTokenType(String token) {
		return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload()
				.get("type", String.class);
	}
}
