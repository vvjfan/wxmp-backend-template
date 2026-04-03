package com.example.wxmpapidemo.face.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "发起人脸核身响应")
public class StartFaceVerifyResponse {

    @Schema(description = "业务流水号")
    private String verificationId;

    @Schema(description = "微信人脸核身会话唯一标识")
    private String verifyId;

    @Schema(description = "verifyId 有效期（秒）")
    private Integer expiresIn;
}
