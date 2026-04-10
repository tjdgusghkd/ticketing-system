package com.ticketing.queue.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticketing.queue.dto.QueueEnterResponseDto;
import com.ticketing.queue.service.QueueService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/queue")
public class QueueApiController {
	
	private final QueueService queueService;
	
	@PostMapping("/{scheduleNo}/enter")
	public ResponseEntity<QueueEnterResponseDto> queueEnter(Authentication authentication, @PathVariable("scheduleNo") Long scheduleNo) {
		String loginId = authentication.getName();
		return ResponseEntity.ok(queueService.enter(scheduleNo, loginId));
	}
}
