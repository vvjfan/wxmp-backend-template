# 微信小程序后端 API 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现微信小程序登录认证、支付功能的后端 API 及 OpenAPI 文档

**Architecture:** 分层单体 (controller → service → repository)，按业务模块分包

**Tech Stack:** Spring Boot 4.0.5, Java 17, JPA/H2, Redis, Spring Security, JWT, springdoc-openapi, Lombok

---

### Task 1: 基础配置与通用组件

**Files:**
- Create: `wxmp-api-demo/src/main/resources/application.yaml`
- Create: `wxmp-api-demo/src/main/java/com/example/wxmpapidemo/config/WxConfig.java`
- Create: `wxmp-api-demo/src/main/java/com/example/wxmpapidemo/config/JwtConfig.java`
- Create: `wxmp-api-demo/src/main/java/com/example/wxmpapidemo/config/OpenApiConfig.java`
- Create: `wxmp-api-demo/src/main/java/com/example/wxmpapidemo/common/BaseEntity.java`
- Create: `wxmp-api-demo/src/main/java/com/example/wxmpapidemo/common/exception/GlobalExceptionHandler.java`
- Create: `wxmp-api-demo/src/main/java/com/example/wxmpapidemo/common/exception/WxApiException.java`
- Create: `wxmp-api-demo/src/main/java/com/example/wxmpapidemo/common/dto/ApiResponse.java`

- [ ] **Step 1: 配置 application.yaml**

```yaml
spring:
  application:
    name: wxmp-api-demo
  datasource:
    url: jdbc:h2:mem:wxmp;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: true
  h2:
    console:
      enabled: true
      path: /h2-console
  data:
    redis:
      host: localhost
      port: 6379

server:
  port: 8080

wx:
  miniapp:
    app-id: ${WX_MINIAPP_APP_ID:your-app-id}
    app-secret: ${WX_MINIAPP_APP_SECRET:your-app-secret}
    code2session-url: https://api.weixin.qq.com/sns/jscode2session
  pay:
    app-id: ${WX_PAY_APP_ID:your-app-id}
    mch-id: ${WX_PAY_MCH_ID:your-mch-id}
    api-key: ${WX_PAY_API_KEY:your-api-key}
    notify-url: https://your-domain.com/api/payment/notify
    unified-order-url: https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi

jwt:
  secret: ${JWT_SECRET:your-256-bit-secret-key-here-must-be-at-least-32-chars}
  access-token-expiration: 1800000
  refresh-token-expiration: 604800000

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
```

- [ ] **Step 2: 创建 WxConfig 配置类**

```java
package com.example.wxmpapidemo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "wx.miniapp")
public class WxConfig {
    private String appId;
    private String appSecret;
    private String code2sessionUrl;
}
```

- [ ] **Step 3: 创建 JwtConfig 配置类**

```java
package com.example.wxmpapidemo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    private String secret;
    private long accessTokenExpiration;
    private long refreshTokenExpiration;
}
```

- [ ] **Step 4: 创建 OpenApiConfig 配置类**

```java
package com.example.wxmpapidemo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("微信小程序后端 API")
                        .version("1.0.0")
                        .description("微信小程序登录、支付相关接口文档")
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@example.com")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
```

- [ ] **Step 5: 创建 BaseEntity**

```java
package com.example.wxmpapidemo.common;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
```

- [ ] **Step 6: 创建 ApiResponse 通用响应**

```java
package com.example.wxmpapidemo.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一响应格式")
public class ApiResponse<T> {

    @Schema(description = "状态码", example = "200")
    private int code;

    @Schema(description = "消息", example = "success")
    private String message;

    @Schema(description = "响应数据")
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
```

- [ ] **Step 7: 创建 WxApiException**

```java
package com.example.wxmpapidemo.common.exception;

import lombok.Getter;

@Getter
public class WxApiException extends RuntimeException {

    private final int code;

    public WxApiException(int code, String message) {
        super(message);
        this.code = code;
    }

    public WxApiException(String message) {
        this(400, message);
    }
}
```

- [ ] **Step 8: 创建 GlobalExceptionHandler**

```java
package com.example.wxmpapidemo.common.exception;

import com.example.wxmpapidemo.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WxApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleWxApiException(WxApiException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "服务器内部错误"));
    }
}
```

- [ ] **Step 9: 验证编译通过**

```bash
cd wxmp-api-demo && ./mvnw compile -q
```

---

### Task 2: 用户模块 (Entity + Repository)

**Files:**
- Create: `wxmp-api-demo/src/main/java/com/example/wxmpapidemo/user/entity/User.java`
- Create: `wxmp-api-demo/src/main/java/com/example/wxmpapidemo/user/repository/UserRepository.java`

- [ ] **Step 1: 创建 User 实体**

```java
package com.example.wxmpapidemo.user.entity;

import com.example.wxmpapidemo.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String openid;

    @Column(name = "session_key")
    private String sessionKey;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "avatar_url")
    private String avatarUrl;
}
```

- [ ] **Step 2: 创建 UserRepository**

```java
package com.example.wxmpapidemo.user.repository;

import com.example.wxmpapidemo.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByOpenid(String openid);
    Optional<User> findByPhoneNumber(String phoneNumber);
    boolean existsByOpenid(String openid);
}
```

- [ ] **Step 3: 验证编译通过**

```bash
cd wxmp-api-demo && ./mvnw compile -q
```

---

### Task 3: JWT 工具类

**Files:**
- Create: `wxmp-api-demo/src/main/java/com/example/wxmpapidemo/auth/util/JwtUtil.java`

- [ ] **Step 1: 添加 jjwt 依赖到 pom.xml**

在 `<properties>` 后添加版本属性：
```xml
<jjwt.version>0.12.5</jjwt.version>
```

在 `<dependencies>` 中添加：
```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>${jjwt.version}</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>${jjwt.version}</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>${jjwt.version}</version>
    <scope>runtime</scope>
</dependency>
```

- [ ] **Step 2: 创建 JwtUtil**

```java
package com.example.wxmpapidemo.auth.util;

import com.example.wxmpapidemo.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private final JwtConfig jwtConfig;
    private final SecretKey signingKey;

    public JwtUtil(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
        this.signingKey = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Long userId, String openid) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("openid", openid);
        return Jwts.builder()
                .claims(claims)
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtConfig.getAccessTokenExpiration()))
                .signWith(signingKey)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtConfig.getRefreshTokenExpiration()))
                .signWith(signingKey)
                .compact();
    }

    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractOpenid(String token) {
        return extractClaim(token, claims -> claims.get("openid", String.class));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

- [ ] **Step 3: 验证编译通过**

```bash
cd wxmp-api-demo && ./mvnw compile -q
```

---

### Task 4: Spring Security 配置与 JWT 过滤器

**Files:**
- Create: `wxmp-api-demo/src/main/java/com/example/wxmpapidemo/auth/filter/JwtAuthenticationFilter.java`
- Create: `wxmp-api-demo/src/main/java/com/example/wxmpapidemo/config/SecurityConfig.java`

- [ ] **Step 1: 创建 JwtAuthenticationFilter**

```java
package com.example.wxmpapidemo.auth.filter;

import com.example.wxmpapidemo.auth.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        if (jwtUtil.isTokenValid(token)) {
            String userId = jwtUtil.extractUserId(token);
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}
```

- [ ] **Step 2: 创建 SecurityConfig**

```java
package com.example.wxmpapidemo.config;

import com.example.wxmpapidemo.auth.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/login",
                    "/api/auth/refresh",
                    "/api/payment/notify",
                    "/h2-console/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/api-docs/**",
                    "/api-docs"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

- [ ] **Step 3: 验证编译通过**

```bash
cd wxmp-api-demo && ./mvnw compile -q
```

---

### Task 5: 认证模块 - 登录与 Token 刷新

**Files:**
- Create: `wxmp-api-demo/src/main/java/com/example/wxmpapidemo/auth/dto/LoginRequest.java`
- Create: `wxmp-api-demo/src/main/java/com/example/wxmpapidemo/auth/dto/LoginResponse.java`
- Create: `wxmp-api-demo/src/main/java/com/example/wxmpapidemo/auth/dto/RefreshRequest.java`
- Create: `wxmp-api-demo/src/main/java/com/example/wxmpapidemo/auth/dto/RefreshResponse.java`
- Create: `wxmp-api-demo/src/main/java/com/example/wxmpapidemo/auth/dto/PhoneBindRequest.java`
- Create: `wxmp-api-demo/src/main/java/com/example/wxmpapidemo/auth/service/AuthService.java`
- Create: `wxmp-api-demo/src/main/java/com/example/wxmpapidemo/auth/controller/AuthController.java`

- [ ] **Step 1: 创建 DTO 类**

```java
package com.example.wxmpapidemo.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "登录请求")
public class LoginRequest {

    @NotBlank(message = "code 不能为空")
    @Schema(description = "微信登录凭证 code", example = "081xxx")
    private String code;
}
```

```java
package com.example.wxmpapidemo.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginResponse {

    @Schema(description = "访问令牌")
    private String accessToken;

    @Schema(description = "刷新令牌")
    private String refreshToken;

    @Schema(description = "是否新用户")
    private boolean newUser;
}
```

```java
package com.example.wxmpapidemo.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "刷新 Token 请求")
public class RefreshRequest {

    @NotBlank(message = "refreshToken 不能为空")
    @Schema(description = "刷新令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;
}
```

```java
package com.example.wxmpapidemo.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "刷新 Token 响应")
public class RefreshResponse {

    @Schema(description = "新的访问令牌")
    private String accessToken;
}
```

```java
package com.example.wxmpapidemo.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "绑定手机号请求")
public class PhoneBindRequest {

    @NotBlank(message = "code 不能为空")
    @Schema(description = "微信手机号授权 code", example = "081xxx")
    private String code;
}
```

- [ ] **Step 2: 创建 AuthService**

```java
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
                .body(Map.class);
    }

    private String getPhoneNumber(String code, String openid, String sessionKey) {
        // 实际实现需要调用微信 getPhoneNumber API
        // 这里返回模拟值，实际部署时替换为真实调用
        return "13800138000";
    }
}
```

- [ ] **Step 3: 创建 AuthController**

```java
package com.example.wxmpapidemo.auth.controller;

import com.example.wxmpapidemo.auth.dto.LoginRequest;
import com.example.wxmpapidemo.auth.dto.LoginResponse;
import com.example.wxmpapidemo.auth.dto.PhoneBindRequest;
import com.example.wxmpapidemo.auth.dto.RefreshRequest;
import com.example.wxmpapidemo.auth.dto.RefreshResponse;
import com.example.wxmpapidemo.auth.service.AuthService;
import com.example.wxmpapidemo.auth.util.JwtUtil;
import com.example.wxmpapidemo.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证", description = "小程序登录、Token 刷新、手机号绑定接口")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    @Operation(summary = "小程序登录", description = "使用微信登录凭证 code 换取 access_token 和 refresh_token")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新 Token", description = "使用 refresh_token 获取新的 access_token")
    public ApiResponse<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ApiResponse.success(authService.refreshToken(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    @Operation(summary = "登出", description = "使当前 refresh_token 失效")
    public ApiResponse<Void> logout(Authentication authentication,
                                    @RequestHeader("Authorization") String authHeader) {
        String userId = (String) authentication.getPrincipal();
        String refreshToken = authHeader.substring(7);
        authService.logout(userId, refreshToken);
        return ApiResponse.success(null);
    }

    @PostMapping("/phone")
    @Operation(summary = "绑定手机号", description = "使用微信手机号授权 code 绑定手机号")
    public ApiResponse<Void> bindPhone(Authentication authentication,
                                       @Valid @RequestBody PhoneBindRequest request) {
        String userId = (String) authentication.getPrincipal();
        authService.bindPhone(userId, request.getCode());
        return ApiResponse.success("手机号绑定成功", null);
    }
}
```

- [ ] **Step 4: 添加 validation 依赖到 pom.xml**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

- [ ] **Step 5: 验证编译通过**

```bash
cd wxmp-api-demo && ./mvnw compile -q
```

---

### Task 6: 用户模块 - 用户信息接口

**Files:**
- Create: `wxmp-api-demo/src/main/java/com/example/wxmpapidemo/user/dto/UserProfileResponse.java`
- Create: `wxmp-api-demo/src/main/java/com/example/wxmpapidemo/user/dto/UpdateProfileRequest.java`
- Create: `wxmp-api-demo/src/main/java/com/example/wxmpapidemo/user/service/UserService.java`
- Create: `wxmp-api-demo/src/main/java/com/example/wxmpapidemo/user/controller/UserController.java`

- [ ] **Step 1: 创建 DTO 类**

```java
package com.example.wxmpapidemo.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "用户信息响应")
public class UserProfileResponse {

    @Schema(description = "用户 ID")
    private Long id;

    @Schema(description = "微信 openid")
    private String openid;

    @Schema(description = "手机号")
    private String phoneNumber;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像 URL")
    private String avatarUrl;
}
```

```java
package com.example.wxmpapidemo.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新用户信息请求")
public class UpdateProfileRequest {

    @Schema(description = "昵称", example = "微信用户")
    private String nickname;

    @Schema(description = "头像 URL", example = "https://example.com/avatar.jpg")
    private String avatarUrl;
}
```

- [ ] **Step 2: 创建 UserService**

```java
package com.example.wxmpapidemo.user.service;

import com.example.wxmpapidemo.common.exception.WxApiException;
import com.example.wxmpapidemo.user.dto.UpdateProfileRequest;
import com.example.wxmpapidemo.user.dto.UserProfileResponse;
import com.example.wxmpapidemo.user.entity.User;
import com.example.wxmpapidemo.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserProfileResponse getProfile(String userId) {
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new WxApiException(404, "用户不存在"));

        return new UserProfileResponse(
                user.getId(),
                user.getOpenid(),
                user.getPhoneNumber(),
                user.getNickname(),
                user.getAvatarUrl()
        );
    }

    public UserProfileResponse updateProfile(String userId, UpdateProfileRequest request) {
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new WxApiException(404, "用户不存在"));

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        userRepository.save(user);

        return new UserProfileResponse(
                user.getId(),
                user.getOpenid(),
                user.getPhoneNumber(),
                user.getNickname(),
                user.getAvatarUrl()
        );
    }
}
```

- [ ] **Step 3: 创建 UserController**

```java
package com.example.wxmpapidemo.user.controller;

import com.example.wxmpapidemo.common.dto.ApiResponse;
import com.example.wxmpapidemo.user.dto.UpdateProfileRequest;
import com.example.wxmpapidemo.user.dto.UserProfileResponse;
import com.example.wxmpapidemo.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@Tag(name = "用户", description = "用户信息管理接口")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息")
    public ApiResponse<UserProfileResponse> getProfile(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        return ApiResponse.success(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    @Operation(summary = "更新用户信息", description = "更新当前登录用户的昵称和头像")
    public ApiResponse<UserProfileResponse> updateProfile(Authentication authentication,
                                                          @RequestBody UpdateProfileRequest request) {
        String userId = (String) authentication.getPrincipal();
        return ApiResponse.success(userService.updateProfile(userId, request));
    }
}
```

- [ ] **Step 4: 验证编译通过**

```bash
cd wxmp-api-demo && ./mvnw compile -q
```

---

### Task 7: 订单模块 (Entity + Repository)

**Files:**
- Create: `wxmp-api-demo/src/main/java/com/example/wxmpapidemo/payment/entity/OrderStatus.java`
- Create: `wxmp-api-demo/src/main/java/com/example/wxmpapidemo/payment/entity/Order.java`
- Create: `wxmp-api-demo/src/main/java/com/example/wxmpapidemo/payment/repository/OrderRepository.java`

- [ ] **Step 1: 创建 OrderStatus 枚举**

```java
package com.example.wxmpapidemo.payment.entity;

public enum OrderStatus {
    PENDING,
    PAID,
    CLOSED
}
```

- [ ] **Step 2: 创建 Order 实体**

```java
package com.example.wxmpapidemo.payment.entity;

import com.example.wxmpapidemo.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "orders")
public class Order extends BaseEntity {

    @Column(name = "order_no", nullable = false, unique = true)
    private String orderNo;

    @Column(nullable = false)
    private String openid;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "transaction_id")
    private String transactionId;
}
```

- [ ] **Step 3: 创建 OrderRepository**

```java
package com.example.wxmpapidemo.payment.repository;

import com.example.wxmpapidemo.payment.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNo(String orderNo);
}
```

- [ ] **Step 4: 验证编译通过**

```bash
cd wxmp-api-demo && ./mvnw compile -q
```

---

### Task 8: 支付模块 - JSAPI 下单与回调

**Files:**
- Create: `wxmp-api-demo/src/main/java/com/example/wxmpapidemo/payment/dto/CreateOrderRequest.java`
- Create: `wxmp-api-demo/src/main/java/com/example/wxmpapidemo/payment/dto/PayResponse.java`
- Create: `wxmp-api-demo/src/main/java/com/example/wxmpapidemo/payment/dto/OrderResponse.java`
- Create: `wxmp-api-demo/src/main/java/com/example/wxmpapidemo/payment/service/PaymentService.java`
- Create: `wxmp-api-demo/src/main/java/com/example/wxmpapidemo/payment/controller/PaymentController.java`

- [ ] **Step 1: 创建 DTO 类**

```java
package com.example.wxmpapidemo.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "创建订单请求")
public class CreateOrderRequest {

    @NotNull(message = "金额不能为空")
    @Min(value = 1, message = "金额必须大于 0")
    @Schema(description = "订单金额（分）", example = "100")
    private Integer amount;

    @NotBlank(message = "描述不能为空")
    @Schema(description = "订单描述", example = "商品购买")
    private String description;
}
```

```java
package com.example.wxmpapidemo.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "支付响应")
public class PayResponse {

    @Schema(description = "商户订单号")
    private String orderNo;

    @Schema(description = "小程序调起支付所需参数")
    private PayParams payParams;

    @Data
    @AllArgsConstructor
    public static class PayParams {
        @Schema(description = "微信 AppID")
        private String appId;

        @Schema(description = "时间戳", example = "1640000000")
        private String timeStamp;

        @Schema(description = "随机字符串")
        private String nonceStr;

        @Schema(description = "订单详情扩展字符串")
        private String packageValue;

        @Schema(description = "签名类型", example = "RSA")
        private String signType;

        @Schema(description = "签名")
        private String paySign;
    }
}
```

```java
package com.example.wxmpapidemo.payment.dto;

import com.example.wxmpapidemo.payment.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "订单信息响应")
public class OrderResponse {

    @Schema(description = "商户订单号")
    private String orderNo;

    @Schema(description = "订单金额（分）")
    private Integer amount;

    @Schema(description = "订单描述")
    private String description;

    @Schema(description = "订单状态")
    private OrderStatus status;

    @Schema(description = "微信支付单号")
    private String transactionId;
}
```

- [ ] **Step 2: 创建 WxPayConfig 配置类**

```java
package com.example.wxmpapidemo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "wx.pay")
public class WxPayConfig {
    private String appId;
    private String mchId;
    private String apiKey;
    private String notifyUrl;
    private String unifiedOrderUrl;
}
```

- [ ] **Step 3: 创建 PaymentService**

```java
package com.example.wxmpapidemo.payment.service;

import com.example.wxmpapidemo.common.exception.WxApiException;
import com.example.wxmpapidemo.config.WxPayConfig;
import com.example.wxmpapidemo.payment.dto.CreateOrderRequest;
import com.example.wxmpapidemo.payment.dto.OrderResponse;
import com.example.wxmpapidemo.payment.dto.PayResponse;
import com.example.wxmpapidemo.payment.dto.PayResponse.PayParams;
import com.example.wxmpapidemo.payment.entity.Order;
import com.example.wxmpapidemo.payment.entity.OrderStatus;
import com.example.wxmpapidemo.payment.repository.OrderRepository;
import com.example.wxmpapidemo.user.entity.User;
import com.example.wxmpapidemo.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final WxPayConfig wxPayConfig;

    public PaymentService(OrderRepository orderRepository,
                          UserRepository userRepository,
                          WxPayConfig wxPayConfig) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.wxPayConfig = wxPayConfig;
    }

    public PayResponse createJsapiOrder(String userId, CreateOrderRequest request) {
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new WxApiException(404, "用户不存在"));

        if (user.getOpenid() == null) {
            throw new WxApiException(400, "用户未登录");
        }

        String orderNo = generateOrderNo();

        Order order = Order.builder()
                .orderNo(orderNo)
                .openid(user.getOpenid())
                .amount(request.getAmount())
                .description(request.getDescription())
                .status(OrderStatus.PENDING)
                .build();

        orderRepository.save(order);

        // 调用微信统一下单 API
        Map<String, Object> prepayResult = callUnifiedOrder(order, user.getOpenid());
        String prepayId = (String) prepayResult.get("prepay_id");

        if (prepayId == null) {
            throw new WxApiException(500, "微信下单失败");
        }

        // 生成小程序调起支付参数
        PayParams payParams = buildPayParams(prepayId);

        return new PayResponse(orderNo, payParams);
    }

    public void handlePayNotify(Map<String, Object> notifyData) {
        // 实际实现需要验签和解密
        String orderNo = (String) notifyData.get("out_trade_no");
        String transactionId = (String) notifyData.get("transaction_id");

        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new WxApiException(404, "订单不存在"));

        order.setStatus(OrderStatus.PAID);
        order.setTransactionId(transactionId);
        orderRepository.save(order);

        log.info("订单 {} 支付成功，微信交易号: {}", orderNo, transactionId);
    }

    public OrderResponse getOrder(String orderNo, String userId) {
        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new WxApiException(404, "订单不存在"));

        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new WxApiException(404, "用户不存在"));

        if (!order.getOpenid().equals(user.getOpenid())) {
            throw new WxApiException(403, "无权查看此订单");
        }

        return new OrderResponse(
                order.getOrderNo(),
                order.getAmount(),
                order.getDescription(),
                order.getStatus(),
                order.getTransactionId()
        );
    }

    private String generateOrderNo() {
        return System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8);
    }

    private Map<String, Object> callUnifiedOrder(Order order, String openid) {
        // 实际实现需要调用微信统一下单 API
        // 这里返回模拟值，实际部署时替换为真实调用
        return Map.of("prepay_id", "wx" + System.currentTimeMillis());
    }

    private PayParams buildPayParams(String prepayId) {
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonceStr = UUID.randomUUID().toString().replace("-", "");
        String packageValue = "prepay_id=" + prepayId;

        // 实际实现需要签名
        String paySign = "mock_sign_" + System.currentTimeMillis();

        return new PayParams(
                wxPayConfig.getAppId(),
                timeStamp,
                nonceStr,
                packageValue,
                "RSA",
                paySign
        );
    }
}
```

- [ ] **Step 4: 创建 PaymentController**

```java
package com.example.wxmpapidemo.payment.controller;

import com.example.wxmpapidemo.common.dto.ApiResponse;
import com.example.wxmpapidemo.payment.dto.CreateOrderRequest;
import com.example.wxmpapidemo.payment.dto.OrderResponse;
import com.example.wxmpapidemo.payment.dto.PayResponse;
import com.example.wxmpapidemo.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@Tag(name = "支付", description = "微信支付相关接口")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/jsapi")
    @Operation(summary = "JSAPI 下单", description = "创建订单并返回小程序调起支付所需参数")
    public ApiResponse<PayResponse> createOrder(Authentication authentication,
                                                @Valid @RequestBody CreateOrderRequest request) {
        String userId = (String) authentication.getPrincipal();
        return ApiResponse.success(paymentService.createJsapiOrder(userId, request));
    }

    @PostMapping("/notify")
    @Operation(summary = "支付回调通知", description = "接收微信支付结果通知")
    public Map<String, String> handleNotify(@RequestBody Map<String, Object> notifyData) {
        paymentService.handlePayNotify(notifyData);
        return Map.of("code", "SUCCESS", "message", "成功");
    }

    @GetMapping("/order/{orderNo}")
    @Operation(summary = "查询订单", description = "根据订单号查询订单状态")
    public ApiResponse<OrderResponse> getOrder(Authentication authentication,
                                               @PathVariable String orderNo) {
        String userId = (String) authentication.getPrincipal();
        return ApiResponse.success(paymentService.getOrder(orderNo, userId));
    }
}
```

- [ ] **Step 5: 验证编译通过**

```bash
cd wxmp-api-demo && ./mvnw compile -q
```

---

### Task 9: 验证与测试

- [ ] **Step 1: 运行完整编译**

```bash
cd wxmp-api-demo && ./mvnw clean compile -q
```

- [ ] **Step 2: 启动应用验证**

```bash
cd wxmp-api-demo && ./mvnw spring-boot:run
```

- [ ] **Step 3: 验证 OpenAPI 文档可访问**

访问 `http://localhost:8080/swagger-ui.html` 确认 API 文档正常显示。

---
