package com.ticketing.seat.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketing.concert.entity.ConcertSchedule;
import com.ticketing.concert.repository.ConcertScheduleRepository;
import com.ticketing.seat.dto.SeatPageResponseDto;
import com.ticketing.seat.dto.SeatReserveRequestDto;
import com.ticketing.seat.dto.SeatResponseDto;
import com.ticketing.seat.entity.ScheduleSeat;
import com.ticketing.seat.enums.ScheduleSeatStatus;
import com.ticketing.seat.repository.ScheduleSeatRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeatService {

	private final ConcertScheduleRepository concertScheduleRepository;
	private final ScheduleSeatRepository scheduleSeatRepository;
	private final StringRedisTemplate stringRedisTemplate;

	@Transactional
	public SeatPageResponseDto getSeatPageInfo(Long scheduleNo) {
		ConcertSchedule schedule = concertScheduleRepository.findDetailByScheduleNo(scheduleNo)
				.orElseThrow(() -> new IllegalArgumentException("해당 회차가 존재하지 않습니다."));

		String concertTitle = schedule.getConcert().getTitle();

		String scheduleDateTime = formatDateTime(schedule.getStartTime());

		return new SeatPageResponseDto(concertTitle, scheduleDateTime);
	}

	private String formatDateTime(LocalDateTime dateTime) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		return dateTime.format(formatter);
	}

	public List<SeatResponseDto> getSeats(Long scheduleNo, String loginId) {
		List<ScheduleSeat> seats = scheduleSeatRepository.findByScheduleNo(scheduleNo);

		return seats.stream().map(scheduleSeat -> {
			String key = "seat:hold:" + scheduleSeat.getScheduleSeatNo();
			String holder = stringRedisTemplate.opsForValue().get(key);

			boolean booked = scheduleSeat.getStatus() == ScheduleSeatStatus.BOOKED;
			boolean held = holder != null;
			boolean holdByMe = holder != null && holder.equals(loginId);

			return new SeatResponseDto(scheduleSeat.getScheduleSeatNo(), 
					scheduleSeat.getSeat().getSeatNumber(),
					scheduleSeat.getSeat().getSection(), 
					scheduleSeat.getSeat().getRowNum(), 
					scheduleSeat.getPrice(),
					booked, 
					held, 
					holdByMe);
		}).toList();
	}

	public void reserve(Long scheduleNo, String name, SeatReserveRequestDto request) {
		// TODO Auto-generated method stub

	}

	@Transactional
	public void holdSeat(Long scheduleNo, Long scheduleSeatNo, String loginId) {
		ScheduleSeat scheduleSeat = scheduleSeatRepository
				.findByScheduleNoAndScheduleSeatNoForUpdate(scheduleNo, scheduleSeatNo)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석입니다."));

		if (scheduleSeat.getStatus() == ScheduleSeatStatus.BOOKED) {
			throw new IllegalStateException("이미 예약된 좌석입니다.");
		}

		String key = "seat:hold:" + scheduleSeatNo;
		String holder = stringRedisTemplate.opsForValue().get(key);

		System.out.println("loginId = " + loginId);
		System.out.println("holder = " + holder);
		System.out.println("scheduleSeatNo = " + scheduleSeatNo);

		if (holder != null && !holder.equals(loginId)) {
			throw new IllegalStateException("다른 사용자가 선택 중인 좌석입니다.");
		}

		stringRedisTemplate.opsForValue().set(key, loginId, 5, TimeUnit.MINUTES);
	}

}
