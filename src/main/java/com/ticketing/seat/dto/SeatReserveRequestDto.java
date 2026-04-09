package com.ticketing.seat.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SeatReserveRequestDto {
	private Long scheduleNo;
	private String loginId; 
    private List<Long> seatIds;
}
