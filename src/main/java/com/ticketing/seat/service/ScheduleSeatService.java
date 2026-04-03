package com.ticketing.seat.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ticketing.seat.dto.ScheduleSeatResponseDto;
import com.ticketing.seat.repository.ScheduleSeatRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleSeatService {
	
	private final ScheduleSeatRepository scheduleSeatRepository;
	
	public List<ScheduleSeatResponseDto> getScheduleSeats(Long scheduleNo) {
		return scheduleSeatRepository.findByScheduleNo(scheduleNo)
				.stream()
				.map(ScheduleSeatResponseDto::new)
				.toList();
	}
	

}
