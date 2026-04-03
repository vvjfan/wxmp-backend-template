package com.example.wxmpapidemo.face.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "发起人脸核身请求")
public class StartFaceVerifyRequest {

    @NotBlank(message = "姓名不能为空")
    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @NotBlank(message = "身份证号不能为空")
    @Schema(description = "身份证号码", example = "110101199001011234")
    private String idNumber;
}
