package com.ticketing.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class MyReservationResponseDto {

    private Long reservationNo;
    private String concertTitle;
    private String artist;
    private String posterUrl;
    private String scheduleDateTime;

    private String seatSummary;   // "S구역 F열 6번 외 3석"
    private Integer seatCount;    // 4
    private Long totalPrice;
}