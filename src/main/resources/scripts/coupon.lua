-- 유저가 이미 쿠폰을 발급받았는지 확인
if redis.call("EXISTS", KEYS[2]) == 1 then
    return 1
end

-- 쿠폰 재고 확인 후 감소
if redis.call("GET", KEYS[1]) > "0" then
    redis.call("DECR", KEYS[1])
    redis.call("SET", KEYS[2], "1")  -- 유저 발급 기록 저장
    return 0
else
    return 2
end