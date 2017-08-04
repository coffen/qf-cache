local key   = KEYS[1];
local value = KEYS[2];
local ttl   = KEYS[3];

local lock = redis.call('setnx', key, value);
 
if lock == 1 then
	redis.call('expire', key, ttl);
end;

return lock