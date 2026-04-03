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
