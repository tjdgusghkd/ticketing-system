package com.ticketing.concert.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.ticketing.concert.entity.ConcertSchedule;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ConcertScheduleResponseDto {

    private Long scheduleNo;
    private LocalDate concertDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;

    public ConcertScheduleResponseDto(ConcertSchedule schedule) {
        this.scheduleNo = schedule.getScheduleNo();
        this.concertDate = schedule.getStartTime().toLocalDate();
        this.startTime = schedule.getStartTime().toLocalTime();
        this.endTime = schedule.getEndTime().toLocalTime();
        this.status = schedule.getStatus().name();
    }
}