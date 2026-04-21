package com.ticketing.global.exception;

public class HoldNotFoundException extends BusinessException {
    public HoldNotFoundException() {
        super(ErrorCode.HOLD_NOT_FOUND);
    }
}
