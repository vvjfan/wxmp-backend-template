package com.example.wxmpapidemo.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "创建订单请求")
public class CreateOrderRequest {

    @NotNull(message = "金额不能为空")
    @Min(value = 1, message = "金额必须大于 0")
    @Schema(description = "订单金额（分）", example = "100")
    private Integer amount;

    @NotBlank(message = "描述不能为空")
    @Schema(description = "订单描述", example = "商品购买")
    private String description;
}
