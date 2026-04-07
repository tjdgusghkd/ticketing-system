package com.ticketing.concert.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ticketing.concert.dto.ConcertDetailResponseDto;
import com.ticketing.concert.entity.Concert;
import com.ticketing.concert.repository.ConcertRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConcertService {
	private final ConcertRepository concertRepository;
	public List<Concert> findAllWithSchedules() {
		return concertRepository.findAllWithdSchedules();
	}
	
	public ConcertDetailResponseDto getConcertDetail(Long concertNo) {
        Concert concert = concertRepository.findDetailByConcertNo(concertNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 공연이 존재하지 않습니다."));

        return new ConcertDetailResponseDto(concert);
    }

}
