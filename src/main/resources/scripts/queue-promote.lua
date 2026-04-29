	local activeKey = KEYS[1]
	local waitKey = KEYS[2]
	
	local maxCapacity = tonumber(ARGV[1])
	
	local activeCount = redis.call('SCARD', activeKey)
	
	if activeCount >= maxCapacity then
		return nil
	end
	
	local nextUsers = redis.call('ZRANGE', waitKey, 0, 0)
	
	if not nextUsers or #nextUsers == 0 then
		return nil
	end
	
	local nextLoginId = nextUsers[1]
	
	redis.call('ZREM', waitKey, nextLoginId)
	redis.call('SADD', activeKey, nextLoginId)
	
	return nextLoginId