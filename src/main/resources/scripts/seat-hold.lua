local seatHoldKey = KEYS[1]
local userHoldKey = KEYS[2]

local loginId = ARGV[1]
local seatNo = ARGV[2]
local ttlSeconds = tonumber(ARGV[3])

local holder = redis.call('GET', seatHoldKey)

if holder and holder ~= loginId then
	return 0
end

redis.call('SET', seatHoldKey, loginId, 'EX', ttlSeconds)
redis.call('SADD', userHoldKey, seatNo)

return 1