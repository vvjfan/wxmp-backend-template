package com.example.wxmpapidemo.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "绑定手机号请求")
public class PhoneBindRequest {

    @NotBlank(message = "code不能为空")
    @Schema(description = "微信手机号授权 code", example = "081xxx")
    private String code;
}
