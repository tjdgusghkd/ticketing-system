  local activeKey = KEYS[1]
  local waitKey = KEYS[2]

  local loginId = ARGV[1]
  local maxCapacity = tonumber(ARGV[2])
  local now = tonumber(ARGV[3])

  -- 이미 actvive에 있으면 바로 입장 허용 
  if redis.call('SISMEMBER', activeKey, loginId) == 1 then
      return {1, 0}
  end
  -- wait에 없으면 기본적으로 rank 1 반환
  local existingRank = redis.call('ZRANK', waitKey,
  loginId)
  if existingRank then
      return {0, existingRank + 1}
  end

  -- active 인원 수 확인 
  local activeCount = redis.call('SCARD', activeKey)

  -- 내가 대기열 1등이고 active에 자리가 있으면 승격 
  if activeCount < maxCapacity then
      redis.call('SADD', activeKey, loginId)
      return {1, 0}
  else
	-- 아직 대기면 현재 순번 반환
      redis.call('ZADD', waitKey, now, loginId)
      local rank = redis.call('ZRANK', waitKey, loginId)
      return {0, rank + 1}
  end
  	-- ex){1, 0} // 1 -> 입장 가능 여부, 0 -> 대기 순번