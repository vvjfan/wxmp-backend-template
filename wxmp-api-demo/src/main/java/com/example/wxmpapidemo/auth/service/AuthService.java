package com.example.wxmpapidemo.auth.service;

import com.example.wxmpapidemo.auth.dto.LoginRequest;
import com.example.wxmpapidemo.auth.dto.LoginResponse;
import com.example.wxmpapidemo.auth.dto.RefreshResponse;
import com.example.wxmpapidemo.auth.util.JwtUtil;
import com.example.wxmpapidemo.common.exception.WxApiException;
import com.example.wxmpapidemo.config.JwtConfig;
import com.example.wxmpapidemo.config.WxConfig;
import com.example.wxmpapidemo.user.entity.User;
import com.example.wxmpapidemo.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final WxConfig wxConfig;
    private final JwtConfig jwtConfig;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final RestClient restClient;

    public AuthService(UserRepository userRepository,
                       WxConfig wxConfig,
                       JwtConfig jwtConfig,
                       JwtUtil jwtUtil,
                       StringRedisTemplate redisTemplate) {
        this.userRepository = userRepository;
        this.wxConfig = wxConfig;
        this.jwtConfig = jwtConfig;
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
        this.restClient = RestClient.builder().build();
    }

    public LoginResponse login(LoginRequest request) {
        Map<String, Object> sessionData = callCode2Session(request.getCode());

        String openid = (String) sessionData.get("openid");
        String sessionKey = (String) sessionData.get("session_key");

        if (openid == null) {
            throw new WxApiException(400, "微信登录失败，code 无效或已过期");
        }

        User user = userRepository.findByOpenid(openid).orElse(null);
        boolean newUser = false;

        if (user == null) {
            user = User.builder()
                    .openid(openid)
                    .sessionKey(sessionKey)
                    .build();
            userRepository.save(user);
            newUser = true;
        } else {
            user.setSessionKey(sessionKey);
            userRepository.save(user);
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId(), openid);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        redisTemplate.opsForValue().set(
                "refresh:" + user.getId(),
                refreshToken,
                jwtConfig.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );

        return new LoginResponse(accessToken, refreshToken, newUser);
    }

    public RefreshResponse refreshToken(String refreshToken) {
        if (!jwtUtil.isTokenValid(refreshToken)) {
            throw new WxApiException(401, "refreshToken 无效或已过期");
        }

        String userId = jwtUtil.extractUserId(refreshToken);
        String storedToken = redisTemplate.opsForValue().get("refresh:" + userId);

        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new WxApiException(401, "refreshToken 已被吊销");
        }

        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new WxApiException(404, "用户不存在"));

        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getOpenid());
        return new RefreshResponse(newAccessToken);
    }

    public void logout(String userId, String refreshToken) {
        redisTemplate.delete("refresh:" + userId);
    }

    public void bindPhone(String userId, String code) {
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new WxApiException(404, "用户不存在"));

        if (user.getPhoneNumber() != null) {
            throw new WxApiException(400, "已绑定手机号");
        }

        String phoneNumber = getPhoneNumber(code, user.getOpenid(), user.getSessionKey());
        user.setPhoneNumber(phoneNumber);
        userRepository.save(user);
    }

    private Map<String, Object> callCode2Session(String code) {
        String url = wxConfig.getCode2SessionUrl()
                + "?appid=" + wxConfig.getAppId()
                + "&secret=" + wxConfig.getAppSecret()
                + "&js_code=" + code
                + "&grant_type=authorization_code";

        return restClient.get()
                .uri(url)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    private String getPhoneNumber(String code, String openid, String sessionKey) {
        return "13800138000";
    }
}
