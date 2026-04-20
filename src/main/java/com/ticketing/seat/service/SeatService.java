package com.ticketing.seat.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketing.concert.entity.ConcertSchedule;
import com.ticketing.concert.repository.ConcertScheduleRepository;
import com.ticketing.member.entity.Member;
import com.ticketing.member.repository.MemberRepository;
import com.ticketing.reservation.entity.Reservation;
import com.ticketing.reservation.enums.ReservationStatus;
import com.ticketing.reservation.repository.ReservationRepository;
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
	private final MemberRepository memberRepository;
	private final ReservationRepository reservationRepository;

	@Transactional(readOnly = true)
	public SeatPageResponseDto getSeatPageInfo(Long scheduleNo) {
		ConcertSchedule schedule = concertScheduleRepository.findDetailByScheduleNo(scheduleNo)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회차입니다."));

		return new SeatPageResponseDto(schedule.getConcert().getTitle(), formatDateTime(schedule.getStartTime()));
	}

	public List<SeatResponseDto> getSeats(Long scheduleNo, String loginId) {
		List<ScheduleSeat> seats = scheduleSeatRepository.findByScheduleNo(scheduleNo); // 모든 좌석들을 db에서 가져옴 

		List<String> keys = seats.stream()
				.map(seat -> holdKey(scheduleNo, seat.getScheduleSeatNo())).toList();
		if(seats.isEmpty()) {
			return List.of();
		}
		DefaultRedisScript<List> script = new DefaultRedisScript<>();
		script.setLocation(new ClassPathResource("scripts/seat-hold-status.lua"));
		script.setResultType(List.class);

		List<?> redisResult = stringRedisTemplate.execute(script, keys, loginId);

		if(redisResult == null || redisResult.size() != seats.size() * 2) {
			throw new IllegalStateException("좌석 조회 결과가 올바르지 않습니다.");
		}
		return buildSeatResponses(seats, redisResult, loginId);
	}

	private List<SeatResponseDto> buildSeatResponses(List<ScheduleSeat> seats, List<?> redisResult, String loginId) {
		return java.util.stream.IntStream.range(0, seats.size()).mapToObj(i -> {
			ScheduleSeat scheduleSeat = seats.get(i);
			// [holder1, pttl, holder2, pttl...]
			// - redisResult.get(i * 2) 방식은 Lua 반환 형식이 바뀌면 바로 깨짐
			// - 그래서 이 메소드는 seat 1개당 [holder, ttl] 2칸 고정이라는 전제가 강함
			String holder = toNullableString(redisResult.get(i * 2));
			Long holdExpiresInSeconds = toHoldExpireSeconds(redisResult.get(i * 2 + 1));

			boolean booked = scheduleSeat.getStatus() == ScheduleSeatStatus.BOOKED;
			boolean held = holder != null;
			boolean holdByMe = holder != null && holder.equals(loginId);

			return new SeatResponseDto(
					scheduleSeat.getScheduleSeatNo(), 
					scheduleSeat.getSeat().getSeatNumber(),
					scheduleSeat.getSeat().getSection(), 
					scheduleSeat.getSeat().getRowNum(), 
					scheduleSeat.getPrice(),
					booked, 
					held, 
					holdByMe, 
					holdExpiresInSeconds
					);
		}).toList();
	}

	private String toNullableString(Object value) {
		if (value == null) {
			return null;
		}

		String str = value.toString();
		return str.isBlank() ? null : str;
	}

	private Long toHoldExpireSeconds(Object value) {
	      if (value == null) {
	          return null;
	      }

	      long pttl = Long.parseLong(value.toString());
	      if (pttl <= 0) {
	          return null;
	      }
	      
	      return (pttl + 999) / 1000;
	}
	@Transactional
	public void reserve(Long scheduleNo, String loginId, SeatReserveRequestDto request) {
		List<Long> seatIds = request.getSeatIds();
		
		int requestedCount = seatIds.size();
		int existingCount = scheduleSeatRepository.countByMemberAndSchedule(loginId, scheduleNo);

		if (requestedCount > 4) {
			throw new IllegalArgumentException("1인당 최대 4개까지만 예약 가능합니다.");
		}

		if (existingCount + requestedCount > 4) {
			throw new IllegalStateException(
					"이미 예약된 좌석을 포함하여 최대 4개까지만 구매 가능합니다. (현재 가능 수량: " + (4 - existingCount) + "개)");
		}

		if (request.getSeatIds() == null || request.getSeatIds().isEmpty()) {
			throw new IllegalArgumentException("선택한 좌석이 없습니다.");
		}

		List<String> keys = seatIds.stream().map(seatId -> holdKey(scheduleNo, seatId)).toList();
		List<String> holders = stringRedisTemplate.opsForValue().multiGet(keys);
		
		for (String holder : holders) {
			if (holder == null || !holder.equals(loginId)) {
				throw new IllegalStateException("선점이 만료되거나 본인이 선점한 좌석이 아닙니다.");
			}
		}

		// JPA 벌크 연산으로 DB 업데이트
		int updateCount = scheduleSeatRepository.updateStatusToBooked(scheduleNo, seatIds);

		if (updateCount != seatIds.size()) {
			throw new IllegalStateException("일부 좌석의 상태를 업데이트 할 수 없습니다.");
		}

		Member member = memberRepository.findByLoginId(loginId)
				.orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
		ConcertSchedule schedule = concertScheduleRepository.findById(scheduleNo)
				.orElseThrow(() -> new IllegalArgumentException("회차 없음"));

		for (Long seatId : seatIds) {
			ScheduleSeat seat = scheduleSeatRepository.findById(seatId).orElseThrow();

			Reservation reservation = Reservation.builder().member(member).scheduleSeat(seat).schedule(schedule)
					.reservationStatus(ReservationStatus.RESERVED).build();
			reservationRepository.save(reservation);
		}

		String userHoldKey = userHoldKey(scheduleNo, loginId);
		stringRedisTemplate.delete(keys);
		// userHoldKey의 원소들 삭제(ex {10, 11} -> {})
		stringRedisTemplate.opsForSet().remove(
				userHoldKey,
				seatIds.stream().map(String::valueOf).toArray(String[]::new)
				);
		
		Long remainCount = stringRedisTemplate.opsForSet().size(userHoldKey);
		// userHoldKey의 원소가 비어있으면 set key 자체 삭제
		if(remainCount != null && remainCount == 0L) {
			stringRedisTemplate.delete(userHoldKey);
		}
	}

	@Transactional
	public void holdSeat(Long scheduleNo, Long scheduleSeatNo, String loginId) {
		ScheduleSeat scheduleSeat = scheduleSeatRepository
				.findByScheduleNoAndScheduleSeatNoForUpdate(scheduleNo, scheduleSeatNo)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석입니다."));
		List<Reservation> reservationList = reservationRepository.findByMember_LoginIdAndSchedule_ScheduleNo(loginId,
				scheduleNo);
		int dbCount = reservationList.size();

		if (scheduleSeat.getStatus() == ScheduleSeatStatus.BOOKED) {
			throw new IllegalStateException("이미 예약된 좌석입니다.");
		}

		// N유저가 몇개의 좌석을 잡고 있는지
		String userHoldKey = userHoldKey(scheduleNo, loginId);
		Long holdCount = stringRedisTemplate.opsForSet().size(userHoldKey);
		long currentHoldCount = holdCount != null ? holdCount : 0L;
		
		if(currentHoldCount + dbCount+ 1 > 4) {
			throw new IllegalStateException("1인당 최대 4자리까지 예매 가능합니다.");
		}

		
		// 해당 좌석을 누가 잡고 있는지에 대한 key
		String key = holdKey(scheduleNo, scheduleSeatNo);
		String holder = stringRedisTemplate.opsForValue().get(key);

		if (holder != null && !holder.equals(loginId)) {
			throw new IllegalStateException("다른 사용자가 선택 중인 좌석입니다.");
		}

		stringRedisTemplate.opsForValue().set(key, loginId, 5, TimeUnit.MINUTES);
		stringRedisTemplate.opsForSet().add(userHoldKey, String.valueOf(scheduleSeatNo));
	}

	@Transactional
	public void unholdSeat(Long scheduleNo, Long scheduleSeatNo, String loginId) {
		ScheduleSeat scheduleSeat = scheduleSeatRepository
				.findByScheduleNoAndScheduleSeatNoForUpdate(scheduleNo, scheduleSeatNo)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석입니다."));

		String key = holdKey(scheduleSeat.getSchedule().getScheduleNo(), scheduleSeat.getScheduleSeatNo());
		String holder = stringRedisTemplate.opsForValue().get(key);

		if (holder == null) {
			throw new IllegalArgumentException("선점된 좌석이 아닙니다.");
		}

		if (!holder.equals(loginId)) {
			throw new IllegalStateException("본인이 선점한 좌석만 해제할 수 있습니다.");
		}
		
		String userHoldKey = userHoldKey(scheduleNo, loginId);
		
		stringRedisTemplate.delete(key);
		stringRedisTemplate.opsForSet().remove(userHoldKey, String.valueOf(scheduleSeatNo));
		
		Long remainCount = stringRedisTemplate.opsForSet().size(userHoldKey);
		if(remainCount != null && remainCount == 0L) {
			stringRedisTemplate.delete(userHoldKey);
		}
	}

	private String formatDateTime(LocalDateTime dateTime) {
		return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
	}

	private String holdKey(Long scheduleNo, Long scheduleSeatNo) {
		return "seat:hold:" + scheduleNo + ":" + scheduleSeatNo;
	}
	
	private String userHoldKey(Long scheduleNo, String loginId) {
		return "user:hold:" + scheduleNo  + ":" + loginId;
	}
}
