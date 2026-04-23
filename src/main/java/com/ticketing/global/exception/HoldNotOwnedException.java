package com.ticketing.global.exception;

public class HoldNotOwnedException extends BusinessException {
    public HoldNotOwnedException() {
        super(ErrorCode.HOLD_NOT_OWNED);
    }
}
