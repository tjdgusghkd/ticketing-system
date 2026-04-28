package com.ticketing.queue.service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ticketing.queue.dto.QueueEnterResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QueueService {

	private static final int MAX_CAPACITY = 1;

	private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<List> queueEnterScript = createListScript("scripts/queue-enter.lua");
    private final DefaultRedisScript<List> queueStatusScript = createListScript("scripts/queue-status.lua");
    private final DefaultRedisScript<Long> queueLeaveScript = createLongScript("scripts/queue-leave.lua");
	
	public QueueEnterResponseDto enter(Long scheduleNo, String loginId) {
		String activeKey = "active:round:" + scheduleNo;
		String waitKey = "wait:round:" + scheduleNo;
		String activeSchedulesKey = "active:schedules";

		List<?> result = stringRedisTemplate.execute(queueEnterScript, 
				List.of(activeKey, waitKey, activeSchedulesKey), 
				loginId,
				String.valueOf(MAX_CAPACITY),
				String.valueOf(System.currentTimeMillis()),
				String.valueOf(scheduleNo));

		return toResponse(result, "대기열 진입 처리 실패");
	}

	public QueueEnterResponseDto checkQueue(Long scheduleNo, String loginId) {
		String activeKey = "active:round:" + scheduleNo;
		String waitKey = "wait:round:" + scheduleNo;


		List<?> result = stringRedisTemplate.execute(queueStatusScript, List.of(activeKey, waitKey), loginId,
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
		String heartbeatKey = heartbeatKey(scheduleNo, loginId);	
		
		stringRedisTemplate.execute(queueLeaveScript,
				List.of(activeKey, waitKey, userHoldKey, heartbeatKey), 
				loginId, 
				String.valueOf(scheduleNo));
		
		promoteNextWaitingUser(scheduleNo);
	}
	
	
	private String userHoldKey(Long scheduleNo, String loginId) {
		return "user:hold:" + scheduleNo + ":" + loginId;
	}
	
	public void heartbeat(Long scheduleNo, String loginId) {
		String heartbeatKey = heartbeatKey(scheduleNo, loginId);
		stringRedisTemplate.opsForValue().set(heartbeatKey, "alive", 30, TimeUnit.SECONDS);
	}
	
	private String heartbeatKey(Long scheduleNo, String loginId) {
		return "queue:hb:" + scheduleNo + ":" + loginId;
	}
	
	private void cleanupStaleQueueUsers(Long scheduleNo) {
		String activeKey = "active:round:" + scheduleNo;
		String waitKey = "wait:round:" + scheduleNo;
		String activeSchedulesKey = "active:schedules";
		Set<String> activeUsers = stringRedisTemplate.opsForSet().members(activeKey);
		
		
		if(activeUsers != null) {
			for(String loginId : activeUsers) {
				String heartbeatKey = heartbeatKey(scheduleNo, loginId);
				Boolean alive = stringRedisTemplate.hasKey(heartbeatKey);
				if(!Boolean.TRUE.equals(alive)) {
					stringRedisTemplate.opsForSet().remove(activeKey, loginId);
				}
			}
		}
		
		Set<String> waitUsers = stringRedisTemplate.opsForZSet().range(waitKey,0, -1);
		if(waitUsers != null) {
			for(String loginId : waitUsers) {
				String heartbeatKey = heartbeatKey(scheduleNo, loginId);
				Boolean alive = stringRedisTemplate.hasKey(heartbeatKey);
				if(!Boolean.TRUE.equals(alive)) {
					stringRedisTemplate.opsForZSet().remove(waitKey, loginId);
				}
			}
		}
		
		Long activeCount = stringRedisTemplate.opsForSet().size(activeKey);
		Long waitCount = stringRedisTemplate.opsForZSet().zCard(waitKey);
		
		boolean activeEmpty = activeCount == null || activeCount == 0L;
		boolean waitEmpty = waitCount == null || waitCount == 0L;
		
		if(activeEmpty && waitEmpty) {
			stringRedisTemplate.opsForSet().remove(activeSchedulesKey, String.valueOf(scheduleNo));
		}
	}
	
	@Scheduled(fixedDelay = 5000)
	public void scheduledCleanup() {
	    Set<String> activeScheduleIds = stringRedisTemplate.opsForSet().members("active:schedules");

	    if (activeScheduleIds == null || activeScheduleIds.isEmpty()) return;

	    for (String scheduleId : activeScheduleIds) {
	        Long scheduleNo = Long.valueOf(scheduleId);
	        
	        // 2. 기존에 만드신 청소 로직 실행
	        cleanupStaleQueueUsers(scheduleNo);
	        promoteNextWaitingUser(scheduleNo);
	    }
	}
	
	private DefaultRedisScript<List> createListScript(String path) {
		DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource(path));
        script.setResultType(List.class);
        return script;
    }

    private DefaultRedisScript<Long> createLongScript(String path) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource(path));
        script.setResultType(Long.class);
        return script;
    }
    
    private void promoteNextWaitingUser(Long scheduleNo) {
    	String activeKey = "active:round:" + scheduleNo;
    	String waitKey = "wait:round:" + scheduleNo;
    	
    	Long activeCount = stringRedisTemplate.opsForSet().size(activeKey);
    	if(activeCount != null && activeCount >= MAX_CAPACITY) {
    		return;
    	}
    	
    	Set<String> nextUsers = stringRedisTemplate.opsForZSet().range(waitKey,0,0);
    	if(nextUsers == null || nextUsers.isEmpty()) {
    		return;
    	}
    	
    	String nextLoginId = nextUsers.iterator().next();
    	
    	stringRedisTemplate.opsForZSet().remove(waitKey,nextLoginId);
    	stringRedisTemplate.opsForSet().add(activeKey, nextLoginId);
	}
}
