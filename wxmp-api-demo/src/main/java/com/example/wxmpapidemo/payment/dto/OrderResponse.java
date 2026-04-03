package com.example.wxmpapidemo.payment.dto;

import com.example.wxmpapidemo.payment.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "订单信息响应")
public class OrderResponse {

    @Schema(description = "商户订单号")
    private String orderNo;

    @Schema(description = "订单金额（分）")
    private Integer amount;

    @Schema(description = "订单描述")
    private String description;

    @Schema(description = "订单状态")
    private OrderStatus status;

    @Schema(description = "微信支付单号")
    private String transactionId;
}
