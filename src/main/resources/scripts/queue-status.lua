	local activeKey = KEYS[1]
	local waitKey = KEYS[2]
	
	local loginId = ARGV[1]
	
	if redis.call('SISMEMBER', activeKey, loginId) == 1
	then
	    return {1, 0}
	end
	
	local rank = redis.call('ZRANK', waitKey, loginId)
	if rank then
	    return {0, rank + 1}
	end
	
	return {0, -1}