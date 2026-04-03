package com.example.wxmpapidemo.face.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "查询人脸核身结果响应")
public class FaceVerifyResultResponse {

    @Schema(description = "业务流水号")
    private String verificationId;

    @Schema(description = "核身状态", example = "SUCCESS")
    private String status;

    @Schema(description = "核身返回码", example = "10000")
    private Integer verifyRet;

    @Schema(description = "错误信息")
    private String errorMsg;
}
