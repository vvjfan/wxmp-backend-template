package com.example.wxmpapidemo.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "支付响应")
public class PayResponse {

    @Schema(description = "商户订单号")
    private String orderNo;

    @Schema(description = "小程序调起支付所需参数")
    private PayParams payParams;

    @Data
    @AllArgsConstructor
    public static class PayParams {
        @Schema(description = "微信 AppID")
        private String appId;

        @Schema(description = "时间戳", example = "1640000000")
        private String timeStamp;

        @Schema(description = "随机字符串")
        private String nonceStr;

        @Schema(description = "订单详情扩展字符串")
        private String packageValue;

        @Schema(description = "签名类型", example = "RSA")
        private String signType;

        @Schema(description = "签名")
        private String paySign;
    }
}
