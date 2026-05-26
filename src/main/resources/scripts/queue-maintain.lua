local activeKey = KEYS[1]
local waitKey = KEYS[2]
local activeSchedulesKey = KEYS[3]

local scheduleNo = ARGV[1]
local maxCapacity = tonumber(ARGV[2])

local activeUsers = redis.call('SMEMBERS', activeKey)
for _, loginId in ipairs(activeUsers) do
    local heartbeatKey = "queue:hb:" .. scheduleNo .. ":" .. loginId
    if redis.call('EXISTS', heartbeatKey) == 0 then
        redis.call('SREM', activeKey, loginId)
    end
end

local waitUsers = redis.call('ZRANGE', waitKey, 0, -1)
for _, loginId in ipairs(waitUsers) do
    local heartbeatKey = "queue:hb:" .. scheduleNo .. ":" .. loginId
    if redis.call('EXISTS', heartbeatKey) == 0 then
        redis.call('ZREM', waitKey, loginId)
    end
end

local activeCount = redis.call('SCARD', activeKey)
while activeCount < maxCapacity do
    local nextUsers = redis.call('ZRANGE', waitKey, 0, 0)
    if not nextUsers or #nextUsers == 0 then
        break
    end

    local nextLoginId = nextUsers[1]
    redis.call('ZREM', waitKey, nextLoginId)
    redis.call('SADD', activeKey, nextLoginId)
    activeCount = activeCount + 1
end

local waitCount = redis.call('ZCARD', waitKey)
if activeCount == 0 and waitCount == 0 then
    redis.call('SREM', activeSchedulesKey, scheduleNo)
end

return tostring(activeCount)
