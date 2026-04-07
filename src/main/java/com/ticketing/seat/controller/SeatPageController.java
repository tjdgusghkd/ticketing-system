package com.ticketing.seat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.ticketing.seat.dto.SeatPageResponseDto;
import com.ticketing.seat.service.SeatService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SeatPageController {
	
	private final SeatService seatService;
	
	@GetMapping("/rounds/{scheduleNo}/seats")
	public String seatSelectPage(@PathVariable("scheduleNo") Long scheduleNo, Model model) {
	    SeatPageResponseDto dto = seatService.getSeatPageInfo(scheduleNo);

	    model.addAttribute("scheduleNo", scheduleNo);
	    model.addAttribute("concertTitle", dto.getConcertTitle());
	    model.addAttribute("scheduleDateTime", dto.getScheduleDateTime());

	    return "seat/select";
	}
}
