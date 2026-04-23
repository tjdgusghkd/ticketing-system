local loginId = ARGV[1]
local result = {}

for i, key in ipairs(KEYS) do
	local holder = redis.call('GET', key)
	
	if not holder then
		table.insert(result, '')
		table.insert(result, '-1')
	else
		table.insert(result, holder)
		
		if holder == loginId then
			local pttl = redis.call('PTTL', key)
			table.insert(result, tostring(pttl))
		else 
			table.insert(result, '-1')
		end
	end
end

return result