local userHoldKey = KEYS[1]
local scheduleNo = ARGV[1]
local seatNoMembers = redis.call('SMEMBERS', userHoldKey)
local remainCount = 0

if #seatNoMembers > 0 then	
	for _, seatNoValue in ipairs(seatNoMembers) do
		local seatHoldKey = "seat:hold:" ..scheduleNo .. ":" .. seatNoValue
		
		if redis.call('EXISTS', seatHoldKey) == 0 then
			redis.call('SREM', userHoldKey, seatNoValue)
		end
	end
end

remainCount = redis.call('SCARD', userHoldKey)

if remainCount == 0 then
	redis.call('DEL', userHoldKey)
end

return remainCount
