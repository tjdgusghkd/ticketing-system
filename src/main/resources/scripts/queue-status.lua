 local activeKey = KEYS[1]
  local waitKey = KEYS[2]

  local loginId = ARGV[1]
  local maxCapacity = tonumber(ARGV[2])

  if redis.call('SISMEMBER', activeKey, loginId) == 1 then
      return {1, 0}
  end

  local rank = redis.call('ZRANK', waitKey, loginId)
  if not rank then
      return {0, 1}
  end

  local activeCount = redis.call('SCARD', activeKey)

  if rank == 0 and activeCount < maxCapacity then
      redis.call('ZREM', waitKey, loginId)
      redis.call('SADD', activeKey, loginId)
      return {1, 0}
  end

  return {0, rank + 1}