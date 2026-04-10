package com.ticketing.queue.service;

import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.ticketing.queue.dto.QueueEnterResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QueueService {

	private static final int MAX_CAPACITY = 3;

	private final StringRedisTemplate stringRedisTemplate;
	/*
	 * - active:round:{scheduleNo} 현재 입장 허용된 사용자들 - wait:round:{scheduleNo} 대기열 -
	 * pass:round:{scheduleNo}:{loginId} 입장 허용 토큰
	 */

	public QueueEnterResponseDto enter(Long scheduleNo, String loginId) {
		String activeKey = "active:round:" + scheduleNo;
		String waitKey = "wait:round:" + scheduleNo;
		
		
		Boolean isActive = stringRedisTemplate.opsForSet().isMember(activeKey,loginId);
		if(Boolean.TRUE.equals(isActive)) {
			return QueueEnterResponseDto.builder()
					.allowed(true)
					.rank(0)
					.build();
		}
		// 현재 활성 유저수 확인 (SCARD(redis명령어): Set의 크기 조회)
		Long activeCount = stringRedisTemplate.opsForSet().size(activeKey);

		// 가용 인원보다 적으면 바로 입장 허용
		if (activeCount != null && activeCount < MAX_CAPACITY) {
			stringRedisTemplate.opsForSet().add(activeKey, loginId);
			return QueueEnterResponseDto.builder()
					.allowed(true)
					.rank(0)
					.build();
		}

		Double score = stringRedisTemplate.opsForZSet().score(waitKey, loginId);

		if (score == null) {
			// 가용 인원이 꽉 찼다면 대기열에 추가
			// 점수로 현재 시간을 주어 순서대로 정렬하게 함
			stringRedisTemplate.opsForZSet().add(waitKey, loginId, System.currentTimeMillis());
		}
		// 내 순서 확인
		Long rank = stringRedisTemplate.opsForZSet().rank(waitKey, loginId);

		return QueueEnterResponseDto.builder()
				.allowed(false)
				.rank(rank != null ? rank.intValue() + 1 : 1).build();
	}

}
