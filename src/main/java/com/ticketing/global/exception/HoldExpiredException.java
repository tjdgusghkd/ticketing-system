package com.ticketing.global.exception;

public class HoldExpiredException extends BusinessException {
    public HoldExpiredException() {
        super(ErrorCode.HOLD_EXPIRED);
    }
}