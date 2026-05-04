local loginId = ARGV[1]
  local scheduleNo = ARGV[2]
  local seatCount = tonumber(ARGV[3])

  local seatNos = {}
  for i = 1, seatCount do
      seatNos[i] = ARGV[3 + i]
  end

  for _, seatNo in ipairs(seatNos) do
      local seatHoldKey = "seat:hold:" ..
  scheduleNo .. ":" .. seatNo
      local holder = redis.call('GET',
  seatHoldKey)

      if not holder then
          return 0
      end

      if holder ~= loginId then
          return 1
      end
  end

  return 2
  
  -- 0 : hold 없음 or 만료 // 1 : 다른 사람 hold / 2 : 검증 성공