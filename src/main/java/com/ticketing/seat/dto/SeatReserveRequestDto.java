package com.ticketing.seat.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SeatReserveRequestDto {
    private List<Long> seatIds;
}
