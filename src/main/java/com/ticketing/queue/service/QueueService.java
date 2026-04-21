package com.ticketing.queue.service;

import java.util.List;
import java.util.Set;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import com.ticketing.queue.dto.QueueEnterResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QueueService {

	private static final int MAX_CAPACITY = 1;

	private final StringRedisTemplate stringRedisTemplate;
	
	public QueueEnterResponseDto enter(Long scheduleNo, String loginId) {
		String activeKey = "active:round:" + scheduleNo;
		String waitKey = "wait:round:" + scheduleNo;

		DefaultRedisScript<List> script = new DefaultRedisScript<>();
		script.setLocation(new ClassPathResource("scripts/queue-enter.lua"));
		script.setResultType(List.class);

		List<?> result = stringRedisTemplate.execute(script, List.of(activeKey, waitKey), loginId,
				String.valueOf(MAX_CAPACITY),
				String.valueOf(System.currentTimeMillis()));

		return toResponse(result, "대기열 진입 처리 실패");
	}

	public QueueEnterResponseDto checkQueue(Long scheduleNo, String loginId) {
		String activeKey = "active:round:" + scheduleNo;
		String waitKey = "wait:round:" + scheduleNo;

		DefaultRedisScript<List> script = new DefaultRedisScript<>();
		script.setLocation(new ClassPathResource("scripts/queue-status.lua"));
		script.setResultType(List.class);

		List<?> result = stringRedisTemplate.execute(script, List.of(activeKey, waitKey), loginId,
				String.valueOf(MAX_CAPACITY),
				String.valueOf(System.currentTimeMillis()));

		return toResponse(result, "대기열 상태 조회 실패");
	}

	private QueueEnterResponseDto toResponse(List<?> result, String message) {
		if (result == null || result.size() < 2) {
			throw new IllegalStateException(message);
		}

		int allowed = ((Number) result.get(0)).intValue();
		int rank = ((Number) result.get(1)).intValue();

		return QueueEnterResponseDto.builder().allowed(allowed == 1).rank(rank).build();
	}
	
	public boolean isAllowed(Long scheduleNo, String loginId) {
		String activeKey = "active:round:" + scheduleNo;
		Boolean isActive = stringRedisTemplate.opsForSet().isMember(activeKey, loginId);
		return Boolean.TRUE.equals(isActive);
	}

	public void leave(Long scheduleNo, String loginId) {
		String activeKey = "active:round:" + scheduleNo;
		String waitKey = "wait:round:" + scheduleNo;
		String userHoldKey = userHoldKey(scheduleNo, loginId);
		
		stringRedisTemplate.opsForSet().remove(activeKey, loginId);
		stringRedisTemplate.opsForZSet().remove(waitKey, loginId);

		Set<String> heldSeatIds = stringRedisTemplate.opsForSet().members(userHoldKey);
		if(heldSeatIds == null || heldSeatIds.isEmpty()) {
			stringRedisTemplate.delete(userHoldKey);
			return;
		}
		
		List<String> holdKeys = heldSeatIds.stream()
							.map(seatId -> holdKey(scheduleNo, Long.valueOf(seatId)))
							.toList();
		
		stringRedisTemplate.delete(holdKeys);
		stringRedisTemplate.delete(userHoldKey);
		
	}
	
	private String holdKey(Long scheduleNo, Long scheduleSeatNo) {
		return "seat:hold:" + scheduleNo + ":" + scheduleSeatNo;
	}
	
	private String userHoldKey(Long scheduleNo, String loginId) {
		return "user:hold:" + scheduleNo + ":" + loginId;
	}
}
