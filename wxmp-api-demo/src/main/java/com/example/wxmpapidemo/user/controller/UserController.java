package com.example.wxmpapidemo.user.controller;

import com.example.wxmpapidemo.common.dto.ApiResponse;
import com.example.wxmpapidemo.user.dto.UpdateProfileRequest;
import com.example.wxmpapidemo.user.dto.UserProfileResponse;
import com.example.wxmpapidemo.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
                                                          @Valid @RequestBody UpdateProfileRequest request) {
        String userId = (String) authentication.getPrincipal();
        return ApiResponse.success(userService.updateProfile(userId, request));
    }
}
