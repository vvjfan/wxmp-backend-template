package com.example.wxmpapidemo.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "通过 openid 发送订阅消息请求")
public class SendByOpenidRequest {

    @NotBlank(message = "openid 不能为空")
    @Schema(description = "用户 openid", example = "oUpF8uMuAJO_M2pxb1Q9zNjWeS6o")
    private String openid;

    @NotBlank(message = "模板 ID 不能为空")
    @Schema(description = "订阅消息模板 ID", example = "abc123")
    private String templateId;

    @Schema(description = "点击消息后跳转的小程序页面路径", example = "/pages/index/index")
    private String page;

    @Schema(description = "跳转小程序类型", allowableValues = {"developer", "trial", "formal"}, example = "formal")
    private String miniprogramState;

    @Schema(description = "进入小程序查看的语言类型", allowableValues = {"zh_CN", "en_US"}, example = "zh_CN")
    private String lang;

    @jakarta.validation.constraints.NotEmpty(message = "模板数据不能为空")
    @Schema(description = "模板内容，key 为模板变量名，value 为对应值")
    private java.util.Map<String, String> data;
}
