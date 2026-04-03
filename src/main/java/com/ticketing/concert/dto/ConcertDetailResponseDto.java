package com.ticketing.concert.dto;

import java.util.List;

import com.ticketing.concert.entity.Concert;
import com.ticketing.concert.entity.ConcertSchedule;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ConcertDetailResponseDto {
	private Long concertNo;
	private String title;
	private String artist;
	private String description;
	private String posterUrl;
	private List<ConcertScheduleResponseDto> schedules;
	
	public ConcertDetailResponseDto(Concert concert) {
		this.concertNo = concert.getConcertNo();
		this.title = concert.getTitle();
		this.artist = concert.getArtist();
		this.description = concert.getDescription();
		this.posterUrl = concert.getPosterUrl();
		this.schedules = concert.getSchedules().stream()
	                .map(ConcertScheduleResponseDto::new)
	                .toList();
		
	}
}
