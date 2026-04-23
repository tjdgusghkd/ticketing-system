package com.ticketing.queue.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class QueueServiceIntegrationTest {
	
	@Autowired
	private QueueService queueService;
	
	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	
	@BeforeEach
	  void clearRedis() {
	      Set<String> seatHoldKeys = stringRedisTemplate.keys("seat:hold:*");
	      if (seatHoldKeys != null && !seatHoldKeys.isEmpty()) {
	          stringRedisTemplate.delete(seatHoldKeys);
	      }

	      Set<String> userHoldKeys = stringRedisTemplate.keys("user:hold:*");
	      if (userHoldKeys != null && !userHoldKeys.isEmpty()) {
	          stringRedisTemplate.delete(userHoldKeys);
	      }

	      Set<String> heartbeatKeys = stringRedisTemplate.keys("queue:hb:*");
	      if (heartbeatKeys != null && !heartbeatKeys.isEmpty()) {
	          stringRedisTemplate.delete(heartbeatKeys);
	      }

	      Set<String> activeKeys = stringRedisTemplate.keys("active:round:*");
	      if (activeKeys != null && !activeKeys.isEmpty()) {
	          stringRedisTemplate.delete(activeKeys);
	      }

	      Set<String> waitKeys = stringRedisTemplate.keys("wait:round:*");
	      if (waitKeys != null && !waitKeys.isEmpty()) {
	          stringRedisTemplate.delete(waitKeys);
	      }
	  }
	@Test
	void heartbeat_호출시_heartbeatKey가_저장된다() {
		Long scheduleNo = 1L;
		String loginId = "ghkd5370";
		
		String heartbeatKey = "queue:hb:" + scheduleNo + ":" + loginId;
		queueService.heartbeat(scheduleNo, loginId);
		
		Boolean heartbeatExists = stringRedisTemplate.hasKey(heartbeatKey);
		
		Long ttl = stringRedisTemplate.getExpire(heartbeatKey, TimeUnit.SECONDS);
		
		assertThat(Boolean.TRUE.equals(heartbeatExists)).isTrue();
		assertThat(ttl).isPositive();
	}
	
	@Test
	void leave_호출시_active_wait_heartbeat_hold가_정리된다() {
		Long scheduleNo = 2L;
		Long scheduleSeatNo = 110L;
		String loginId = "tjdgus5370";
		
		String activeKey = "active:round:" + scheduleNo;
		String waitKey = "wait:round:" + scheduleNo;
		String heartbeatKey = "queue:hb:" + scheduleNo + ":" + loginId;
		String userHoldKey = "user:hold:" + scheduleNo + ":" + loginId;
		String seatHoldKey = "seat:hold:" + scheduleNo + ":" + scheduleSeatNo;
		
		stringRedisTemplate.opsForSet().add(activeKey, loginId);
		stringRedisTemplate.opsForZSet().add(waitKey,loginId, System.currentTimeMillis());
		stringRedisTemplate.opsForValue().set(heartbeatKey, "alive");
		stringRedisTemplate.opsForSet().add(userHoldKey, String.valueOf(scheduleSeatNo));
		stringRedisTemplate.opsForValue().set(seatHoldKey,loginId);
		
		queueService.leave(scheduleNo, loginId);
		
		Boolean activeContainsUser = stringRedisTemplate.opsForSet().isMember(activeKey, loginId);
        Double waitScore = stringRedisTemplate.opsForZSet().score(waitKey, loginId);
        Boolean heartbeatExists = stringRedisTemplate.hasKey(heartbeatKey);
        Boolean userHoldExists = stringRedisTemplate.hasKey(userHoldKey);
        Boolean seatHoldExists = stringRedisTemplate.hasKey(seatHoldKey);
		
        assertThat(Boolean.TRUE.equals(activeContainsUser)).isFalse();
        assertThat(waitScore).isNull();
        assertThat(Boolean.TRUE.equals(heartbeatExists)).isFalse();
        assertThat(Boolean.TRUE.equals(userHoldExists)).isFalse();
        assertThat(Boolean.TRUE.equals(seatHoldExists)).isFalse();
	}
	
	@Test
	void checkQueue_호출시_heartbeat없는_user는_active_wait에서_제거된다() {
		 Long scheduleNo = 1L;
         String staleActiveUser = "staleActiveUser";
	     String staleWaitUser = "staleWaitUser";
	     String requestUser = "ghkd5370";

	     String activeKey = "active:round:" + scheduleNo;
	     String waitKey = "wait:round:" + scheduleNo;

	     stringRedisTemplate.opsForSet().add(activeKey, staleActiveUser);
	     stringRedisTemplate.opsForZSet().add(waitKey, staleWaitUser, System.currentTimeMillis());

	     queueService.heartbeat(scheduleNo, requestUser);

	     queueService.checkQueue(scheduleNo, requestUser);

	     Boolean activeContainsStaleUser = stringRedisTemplate
	             .opsForSet()
	             .isMember(activeKey, staleActiveUser);

	     Double waitScore = stringRedisTemplate
	             .opsForZSet()
              .score(waitKey, staleWaitUser);
	     
	     assertThat(Boolean.TRUE.equals(activeContainsStaleUser)).isFalse();
	     assertThat(waitScore).isNull();
	}
}
