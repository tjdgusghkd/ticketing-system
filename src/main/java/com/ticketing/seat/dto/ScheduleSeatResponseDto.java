package com.ticketing.seat.dto;

import com.ticketing.seat.entity.ScheduleSeat;

import lombok.Getter;

@Getter
public class ScheduleSeatResponseDto {
    private Long scheduleSeatNo;  // 실제 예약 시 서버로 다시 보낼 ID (PK)
    private String rowNum;        // 좌석 열 (A, B, C...)
    private Integer seatNumber;   // 좌석 번호 (1, 2, 3...)
    private String section;       // 등급 (VIP, R, S)
    private Integer price;        // 해당 좌석 가격
    private String status;        // 좌석 상태 (AVAILABLE, RESERVED, SOLD)

    // Entity -> DTO 변환 생성자
    public ScheduleSeatResponseDto(ScheduleSeat entity) {
        this.scheduleSeatNo = entity.getScheduleSeatNo();
        this.rowNum = entity.getSeat().getRowNum();
        this.seatNumber = entity.getSeat().getSeatNumber();
        this.section = entity.getSeat().getSection();
        this.price = entity.getPrice(); // 회차별 가격인 scheduleSeat의 가격 사용
        this.status = entity.getStatus().name(); // Enum을 String으로 변환
    }
}
