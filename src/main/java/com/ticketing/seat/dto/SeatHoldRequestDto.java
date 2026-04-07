package com.ticketing.seat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SeatHoldRequestDto {
	private Long scheduleNo;
	private Long scheduleSeatNo;
}
