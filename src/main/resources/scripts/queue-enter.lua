  local activeKey = KEYS[1]
  local waitKey = KEYS[2]

  local loginId = ARGV[1]
  local maxCapacity = tonumber(ARGV[2])
  local now = tonumber(ARGV[3])

  -- 이미 actvive에 있으면 바로 입장 허용 
  -- SISMEMBER: Set에 해당 멤버가 있으면 1, 없으면 0 반환
  if redis.call('SISMEMBER', activeKey, loginId) == 1 then
      return {1, 0}
  end
  -- 이미 wait에 있으면 다시 넣지 않고 현재 순번 반환
  -- ZRANK: Sorted Set에서 해당 멤버의 0-based 순위를 반환
  local existingRank = redis.call('ZRANK', waitKey, loginId)
  if existingRank then
      return {0, existingRank + 1}
  end

  -- active 인원 수 확인 
  -- SCARD: Set의 멤버 수 반환
  local activeCount = redis.call('SCARD', activeKey)

  -- active 자리가 남아 있으면 바로 입장 처리 
  -- SADD : Set에 멤버 추가 
  if activeCount < maxCapacity then    -- 서버 가용인원보다 active 인원이 적을때
      redis.call('SADD', activeKey, loginId) 
      return {1, 0}
  else
	-- 자리가 없으면 wait에 등록
	-- ZADD: Sorted Set에 score와 member를 추가 
	  redis.call('ZADD', waitKey, now, loginId)	
	  
	-- 현재 대기 순번 반환
      local rank = redis.call('ZRANK', waitKey, loginId)
      return {0, rank + 1}
  end
  	-- ex){1, 0} // 1 -> 입장 가능 여부, 0 -> 대기 순번