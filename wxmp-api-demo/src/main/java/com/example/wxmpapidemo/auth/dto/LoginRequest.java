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
