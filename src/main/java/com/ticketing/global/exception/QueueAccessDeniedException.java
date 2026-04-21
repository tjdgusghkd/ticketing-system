package com.ticketing.global.exception;

public class QueueAccessDeniedException extends BusinessException {
    public QueueAccessDeniedException() {
        super(ErrorCode.QUEUE_ACCESS_DENIED);
    }
}