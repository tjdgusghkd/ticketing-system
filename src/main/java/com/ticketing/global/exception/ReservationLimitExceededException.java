package com.ticketing.global.exception;

public class ReservationLimitExceededException extends BusinessException {
    public ReservationLimitExceededException() {
        super(ErrorCode.RESERVATION_LIMIT_EXCEEDED);
    }
}
