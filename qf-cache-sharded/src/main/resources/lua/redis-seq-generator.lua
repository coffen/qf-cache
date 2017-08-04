redis.replicate_commands();

local gap = 999;
local group = KEYS[1];

local now = redis.call('TIME');

local count = tonumber(redis.call('INCRBY', group, 1));
if count == gap then
	redis.call('PEXPIRE', group, 0);
end;

return {tonumber(now[1]), tonumber(now[2]), 0, count}