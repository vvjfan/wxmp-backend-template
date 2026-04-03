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

        return toResponse(user);
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
        return toResponse(user);
    }

    private UserProfileResponse toResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getOpenid(),
                user.getPhoneNumber(),
                user.getNickname(),
                user.getAvatarUrl()
        );
    }
}
