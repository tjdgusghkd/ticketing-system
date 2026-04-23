 local activeKey = KEYS[1]
  local waitKey = KEYS[2]

  local loginId = ARGV[1]
  local maxCapacity = tonumber(ARGV[2])
  local now = tonumber(ARGV[3])

  if redis.call('SISMEMBER', activeKey, loginId) == 1 then
      return {1, 0}
  end

  local rank = redis.call('ZRANK', waitKey, loginId)
  if not rank then
      redis.call('ZADD', waitKey, now, loginId)
	  local realRank = redis.call('ZRANK', waitKey, loginId)
	  
	  return {0, realRank + 1}
  end

  local activeCount = redis.call('SCARD', activeKey)

  if rank == 0 and activeCount < maxCapacity then
      redis.call('ZREM', waitKey, loginId)
      redis.call('SADD', activeKey, loginId)
      return {1, 0}
  end

  return {0, rank + 1}