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
