package com.example.wxmpapidemo.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "发送消息响应")
public class SendMessageResponse {

    @Schema(description = "消息 ID")
    private String messageId;

    @Schema(description = "是否发送成功")
    private boolean success;

    @Schema(description = "错误信息")
    private String errorMsg;
}
