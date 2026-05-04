local userHoldKey = KEYS[1]

local scheduleNo = ARGV[1]
local seatCount = tonumber(ARGV[2])

local seatNos = {}
for i = 1, seatCount do
	seatNos[i] = ARGV[2 + i]
end

for _, seatNo in ipairs(seatNos) do
	local seatHoldKey = "seat:hold:" .. scheduleNo .. ":" .. seatNo
	redis.call('DEL', seatHoldKey)
	redis.call('SREM', userHoldKey, seatNo)
end

local remainCount = redis.call('SCARD', userHoldKey)

if remainCount == 0 then 
	redis.call('DEL', userHoldKey)
end

return 1