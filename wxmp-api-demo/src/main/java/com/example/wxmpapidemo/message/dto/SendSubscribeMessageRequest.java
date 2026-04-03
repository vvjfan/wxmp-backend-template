package com.example.wxmpapidemo.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Map;

@Data
@Schema(description = "发送订阅消息请求")
public class SendSubscribeMessageRequest {

    @NotBlank(message = "模板 ID 不能为空")
    @Schema(description = "订阅消息模板 ID", example = "abc123")
    private String templateId;

    @Schema(description = "点击消息后跳转的小程序页面路径", example = "/pages/index/index")
    private String page;

    @Schema(description = "跳转小程序类型", allowableValues = {"developer", "trial", "formal"}, example = "formal")
    private String miniprogramState;

    @Schema(description = "进入小程序查看的语言类型", allowableValues = {"zh_CN", "en_US"}, example = "zh_CN")
    private String lang;

    @NotEmpty(message = "模板数据不能为空")
    @Schema(description = "模板内容，key 为模板变量名，value 为对应值", example = "{\"thing1\": {\"value\": \"订单已发货\"}, \"time2\": {\"value\": \"2024-01-01 12:00:00\"}}")
    private Map<String, String> data;
}
