local activeKey = KEYS[1]
local waitKey = KEYS[2]
local userHoldKey = KEYS[3]
local heartbeatKey = KEYS[4]

local loginId = ARGV[1]
local scheduleNo = ARGV[2]

redis.call('SREM', activeKey, loginId)
redis.call('ZREM', waitKey, loginId)
redis.call('DEL', heartbeatKey)

local heldSeats = redis.call('SMEMBERS', userHoldKey)
for _, seatId in ipairs(heldSeats) do
	local seatKey = "seat:hold:" .. scheduleNo .. ":" .. seatId
	redis.call('DEL', seatKey)
end

redis.call('DEL', userHoldKey)

return 1

	