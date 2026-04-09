package com.ticketing.seat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.ticketing.reservation.entity.Reservation;
import com.ticketing.reservation.repository.ReservationRepository;
import com.ticketing.seat.dto.SeatReserveRequestDto;
import com.ticketing.seat.entity.ScheduleSeat;
import com.ticketing.seat.enums.ScheduleSeatStatus;
import com.ticketing.seat.repository.ScheduleSeatRepository;

@SpringBootTest
@Transactional
public class SeatServiceConcurrencyTest {
	
	@Autowired
	private SeatService seatService;
	
	@Autowired
	private ScheduleSeatRepository scheduleSeatRepository;
	
	@Autowired
	private ReservationRepository reservationRepository;
	
	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	
	// 같은 scheduleNo, 같은 scheduleSeatNo
	// 사용자 A, B가 동시에 holdSeat()
	// 한 명만 성공해야함
	// "seat:hold:" + scheduleNo + ":" + scheduleSeatNo;
	@BeforeEach
	void clearRedis() {
		Set<String> keys =
				stringRedisTemplate.keys("seat:hold:*");
		if(keys != null && !keys.isEmpty()) {
			stringRedisTemplate.delete(keys);
		}
	}
	@Test
	void 같은_좌석을_동시에_hold하면_한명만_성공() throws Exception {
		// 테스트 준비 (같은 회차, 같은 좌석, 다른 사용자 2명)
		Long scheduleNo = 1L;
		Long scheduleSeatNo = 20L;
		String user1 = "ghkd5370";
		String user2 = "tjdgus5370";
		
		// 작업용 스레드 풀 size 2짜리 생성
		ExecutorService executorService =
				Executors.newFixedThreadPool(2);
		// readyLatch -> 두 스레드가 출발 준비가 끝났는지 확인
		CountDownLatch readyLatch = new CountDownLatch(2);
		
		// startLatch -> 둘 다 준비되면 한 번에 출발
		CountDownLatch startLatch = new CountDownLatch(1);
		
		// doneLatch -> 두 스레드가 모두 끝날 떄까지 기다리기
		CountDownLatch doneLatch = new CountDownLatch(2);
		
		// successCount -> 성공 요청 수 , failCount -> 실패 요청 수
		// AtomicInteger -> 스레드가 2개여서 안전하게 카운트를 올리기 위해서
		AtomicInteger successCount = new AtomicInteger();
		AtomicInteger failCount = new AtomicInteger();
		
		Runnable task1 = () -> {
			try {
				// .countDown() -> 준비됐다고 표시
				readyLatch.countDown();
				
				// .await() -> startLatch의 count가 0이 될 때까지 대기  
				startLatch.await();
				
				// 신호 떨어지면 같은 좌석 hold 시도
				// 성공 시 succesCount++ || 실패 시 failCount++
				seatService.holdSeat(scheduleNo, scheduleSeatNo, user1);
				successCount.incrementAndGet();
			} catch(Exception e) {
				e.printStackTrace();
				failCount.incrementAndGet();
			} finally {
				// 끝났다고 표시
				doneLatch.countDown();
			}
		};
		
		Runnable task2 = () -> {
			try {
				readyLatch.countDown();
				startLatch.await();
				
				seatService.holdSeat(scheduleNo, scheduleSeatNo, user2);
				successCount.incrementAndGet();
			} catch(Exception e) {
				e.printStackTrace();
				failCount.incrementAndGet();
			} finally {
				doneLatch.countDown();
			}
		};
		
		// 실제 실행 
		// 스레드 풀에 task 2개를 넣음
		// 이때 각각 별도 스레드에서 실행됨 
		executorService.submit(task1);
		executorService.submit(task2);
		
		// 두 스레드가 최대한 같은 시점에 시작하도록 신호를 줌
		// 두 스레드가 전부 준비될 때까지 기다렸다가 
		// 그 다음에 한 번에 출발 신호를 줌
		readyLatch.await();
		startLatch.countDown();
		
		// task1, task2가 전부 끝날 때까지 기다림 
		doneLatch.await();
		
		// 결과 검증 
		// 한명만 성공, 한명은 실패
		assertThat(successCount.get()).isEqualTo(1);
		assertThat(failCount.get()).isEqualTo(1);
		
		// 실제 Redis에 선점한 사람이 user1 또는 user2 중 한 명인지 확인 -> 진자 한 사람만 hold를 가져갔는지 확인 
		String holder = stringRedisTemplate.opsForValue().get("seat:hold:" + scheduleNo + ":" + scheduleSeatNo);
		assertThat(holder).isIn(user1, user2);
		
		// 스레드 풀 종료 
		executorService.shutdown();
		
	}
	
	@Test
	void hold한_좌석을_reserve하면_booked_변경_예약저장_redis삭제() {
		Long scheduleNo = 5L;
		Long scheduleSeatNo = 272L;
		String loginId = "tjdgus5370";
		
		seatService.holdSeat(scheduleNo, scheduleSeatNo, loginId);
		
		SeatReserveRequestDto request =new SeatReserveRequestDto(scheduleNo, loginId, List.of(scheduleSeatNo));
		seatService.reserve(scheduleNo, loginId, request);
		
		ScheduleSeat seat = scheduleSeatRepository.findById(scheduleSeatNo).orElseThrow();
		
		assertThat(seat.getStatus()).isEqualTo(ScheduleSeatStatus.BOOKED);
		
		List<Reservation> reservations = reservationRepository.findByMember_LoginIdAndSchedule_ScheduleNo(loginId, scheduleNo);
		assertThat(reservations).isNotEmpty();
		
		String holder = stringRedisTemplate.opsForValue().get("seat:hold:" + scheduleNo + ":" +scheduleSeatNo);
		
		assertThat(holder).isNull();
	}
	
	@Test
	void hold하지_않은_좌석은_reserve할_수_없다() {
		Long scheduleNo = 2L;
		Long scheduleSeatNo = 110L;
		String loginId = "ghkd5370";
		
		SeatReserveRequestDto request = new SeatReserveRequestDto(scheduleNo, loginId, List.of(scheduleSeatNo));
		
		assertThatThrownBy(()-> seatService.reserve(scheduleNo, loginId, request)).isInstanceOf(IllegalStateException.class);
	}
	
	@Test
	void reserve는_한번에_5석_요청하면_실패() {
		Long scheduleNo = 3L;
		String loginId = "ghkd5370";
		
		SeatReserveRequestDto request = new SeatReserveRequestDto(scheduleNo, loginId, List.of(171L, 172L, 173L, 174L, 175L));
		
		assertThatThrownBy(()-> seatService.reserve(scheduleNo,loginId, request)).isInstanceOf(IllegalArgumentException.class);
	}
}
