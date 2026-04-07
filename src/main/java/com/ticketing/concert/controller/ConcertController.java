package com.ticketing.concert.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ticketing.concert.dto.ConcertDetailResponseDto;
import com.ticketing.concert.service.ConcertService;
import com.ticketing.seat.service.ScheduleSeatService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/concerts")
@RequiredArgsConstructor
public class ConcertController {
	
	private final ScheduleSeatService scheduleSeatService;
	private final ConcertService concertService;
	
	/*
	 * @GetMapping("/api/v1/concerts/{scheduleNo}/seats") public ResponseEntity<?>
	 * getSeats(@PathVariable("scheduleNo") Long
	 * scheduleNo, @AuthenticationPrincipal UserDetails userDetails) { //
	 * userDetails가 null이 아니면 인증 성공! System.out.println("인증된 사용자: " +
	 * userDetails.getUsername());
	 * 
	 * // 일단은 더미 데이터 60개를 리스트로 반환하도록 짜보세요. return
	 * ResponseEntity.ok(scheduleSeatService.getScheduleSeats(scheduleNo)); }
	 */
	
	@GetMapping("/{concertNo}")
	public String getConcertInfo(@PathVariable("concertNo") Long concertNo, Model model) {
		ConcertDetailResponseDto concert = concertService.getConcertDetail(concertNo);
		model.addAttribute("concert", concert);
		return "concert/concertDetail";
	}
}
