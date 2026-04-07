package com.ticketing.seat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SeatPageResponseDto {
    private String concertTitle;
    private String scheduleDateTime;
}
