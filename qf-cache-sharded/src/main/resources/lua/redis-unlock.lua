local key   = KEYS[1];
local value = KEYS[2];

local stored = redis.call('get', key);
local count = 0;
 
if stored == value then
	count = redis.call('del', key);
end;

return count