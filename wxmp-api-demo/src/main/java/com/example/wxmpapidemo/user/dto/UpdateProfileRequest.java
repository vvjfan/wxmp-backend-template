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
