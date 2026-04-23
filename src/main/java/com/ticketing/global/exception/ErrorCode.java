package com.ticketing.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_400", "잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 내부 오류가 발생했습니다."),

    SEAT_ALREADY_BOOKED(HttpStatus.CONFLICT, "SEAT_409_1", "이미 예약된 좌석입니다."),
    SEAT_HOLD_CONFLICT(HttpStatus.CONFLICT, "SEAT_409_2", "다른 사용자가 선택 중인 좌석입니다."),
    HOLD_NOT_FOUND(HttpStatus.BAD_REQUEST, "SEAT_400_1", "선점된 좌석이 아닙니다."),
    HOLD_NOT_OWNED(HttpStatus.CONFLICT, "SEAT_409_3", "본인이 선점한 좌석만 처리할 수 있습니다."),
    HOLD_EXPIRED(HttpStatus.CONFLICT, "SEAT_409_4", "선점이 만료되었거나 본인이 선점한 좌석이 아닙니다."),
    RESERVATION_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "RESERVATION_409_1", "최대 예매 가능 수량을 초과했습니다."),

    QUEUE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "QUEUE_403_1", "현재 좌석 페이지 입장 권한이 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_1", "유효하지 않은 토큰입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
