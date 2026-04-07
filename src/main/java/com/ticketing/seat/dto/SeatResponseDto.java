package com.ticketing.seat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SeatResponseDto {

    private Long scheduleSeatNo;
    private Integer seatNumber;
    private String section;
    private String rowNum;
    private Integer price;
    private boolean booked;
    private boolean held;
    private boolean holdByMe;
    private Long holdExpiresInSeconds;
}
