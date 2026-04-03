package com.ticketing.concert.service;

import java.util.List;

import org.springframework.stereotype.Service;

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

}
