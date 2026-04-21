package com.ticketing.global.exception;

public class SeatAlreadyBookedException extends BusinessException {
    public SeatAlreadyBookedException() {
        super(ErrorCode.SEAT_ALREADY_BOOKED);
    }
}
