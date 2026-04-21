package com.ticketing.global.exception;

public class SeatHoldConflictException extends BusinessException {
	public SeatHoldConflictException() {
		super(ErrorCode.SEAT_HOLD_CONFLICT);	
	}
}
