package com.ticketing.seat.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticketing.queue.service.QueueService;
import com.ticketing.seat.dto.SeatReserveRequestDto;
import com.ticketing.seat.dto.SeatResponseDto;
import com.ticketing.seat.service.SeatService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rounds")
public class SeatApiController {

    private final SeatService seatService;
    private final QueueService queueService;
    @GetMapping("/{scheduleNo}/seats")
    public ResponseEntity<List<SeatResponseDto>> getSeats(
            @PathVariable("scheduleNo") Long scheduleNo,
            Authentication authentication) {
    	String loginId = authentication.getName();
    	
    	if(!queueService.isAllowed(scheduleNo, loginId)) {
    		return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    	}
        return ResponseEntity.ok(seatService.getSeats(scheduleNo, authentication.getName()));
    }

    @PostMapping("/{scheduleNo}/reserve")
    public ResponseEntity<String> reserveSeats(
            @PathVariable("scheduleNo") Long scheduleNo,
            @RequestBody SeatReserveRequestDto request,
            Authentication authentication) {
        seatService.reserve(scheduleNo, authentication.getName(), request);
        return ResponseEntity.ok("예약 완료");
    }

    @PostMapping("/{scheduleNo}/hold/{scheduleSeatNo}")
    public ResponseEntity<String> holdSeat(
            @PathVariable("scheduleNo") Long scheduleNo,
            @PathVariable("scheduleSeatNo") Long scheduleSeatNo,
            Authentication authentication) {
    	String loginId = authentication.getName();
    	if (!queueService.isAllowed(scheduleNo, loginId)) {
    	      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    	  }
        seatService.holdSeat(scheduleNo, scheduleSeatNo, authentication.getName());
        return ResponseEntity.ok("좌석 선점 성공");
    }

    @DeleteMapping("/{scheduleNo}/hold/{scheduleSeatNo}")
    public ResponseEntity<String> unholdSeat(
            @PathVariable("scheduleNo") Long scheduleNo,
            @PathVariable("scheduleSeatNo") Long scheduleSeatNo,
            Authentication authentication) {
    	String loginId = authentication.getName();
    	if (!queueService.isAllowed(scheduleNo, loginId)) {
    	      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    	  }
        seatService.unholdSeat(scheduleNo, scheduleSeatNo, authentication.getName());
        return ResponseEntity.ok("좌석 선점 해제 성공");
    }
}
