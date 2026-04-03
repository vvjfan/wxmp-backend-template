package com.example.wxmpapidemo.message.service;

import com.example.wxmpapidemo.config.WxConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class WxAccessTokenService {

    private static final Logger log = LoggerFactory.getLogger(WxAccessTokenService.class);
    private static final String ACCESS_TOKEN_KEY = "wx:access_token";

    private final WxConfig wxConfig;
    private final StringRedisTemplate redisTemplate;
    private final RestClient restClient;

    public WxAccessTokenService(WxConfig wxConfig,
                                StringRedisTemplate redisTemplate) {
        this.wxConfig = wxConfig;
        this.redisTemplate = redisTemplate;
        this.restClient = RestClient.builder().build();
    }

    public String getAccessToken() {
        String token = redisTemplate.opsForValue().get(ACCESS_TOKEN_KEY);
        if (token != null) {
            return token;
        }

        return fetchAccessToken();
    }

    private synchronized String fetchAccessToken() {
        String token = redisTemplate.opsForValue().get(ACCESS_TOKEN_KEY);
        if (token != null) {
            return token;
        }

        String url = "https://api.weixin.qq.com/cgi-bin/stable_token";
        String body = """
                {
                    "grant_type": "client_credential",
                    "appid": "%s",
                    "secret": "%s"
                }
                """.formatted(wxConfig.getAppId(), wxConfig.getAppSecret());

        Map<String, Object> response = restClient.post()
                .uri(url)
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .body(Map.class);

        if (response == null || !response.containsKey("access_token")) {
            String errMsg = (String) response.getOrDefault("errmsg", "unknown error");
            throw new RuntimeException("获取 access_token 失败: " + errMsg);
        }

        String accessToken = (String) response.get("access_token");
        Number expiresIn = (Number) response.get("expires_in");
        long ttl = expiresIn != null ? expiresIn.longValue() - 300 : 7200;

        redisTemplate.opsForValue().set(ACCESS_TOKEN_KEY, accessToken, ttl, TimeUnit.SECONDS);
        log.info("获取微信 access_token 成功，有效期 {} 秒", expiresIn);

        return accessToken;
    }

    public void clearAccessToken() {
        redisTemplate.delete(ACCESS_TOKEN_KEY);
    }
}
