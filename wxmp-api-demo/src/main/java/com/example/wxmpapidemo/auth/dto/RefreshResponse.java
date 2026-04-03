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
