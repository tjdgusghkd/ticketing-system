package com.ticketing.concert.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.ticketing.seat.service.ScheduleSeatService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ConcertController {
	
	private final ScheduleSeatService scheduleSeatService;
	
	@GetMapping("/api/v1/concerts/{scheduleNo}/seats")
	public ResponseEntity<?> getSeats(@PathVariable("scheduleNo") Long scheduleNo, @AuthenticationPrincipal UserDetails userDetails) {
	    // userDetails가 null이 아니면 인증 성공!
	    System.out.println("인증된 사용자: " + userDetails.getUsername());
	    
	    // 일단은 더미 데이터 60개를 리스트로 반환하도록 짜보세요.
	    return ResponseEntity.ok(scheduleSeatService.getScheduleSeats(scheduleNo)); 
	}
}
