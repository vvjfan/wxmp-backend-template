package com.example.wxmpapidemo.payment.controller;

import com.example.wxmpapidemo.common.dto.ApiResponse;
import com.example.wxmpapidemo.payment.dto.CreateOrderRequest;
import com.example.wxmpapidemo.payment.dto.OrderResponse;
import com.example.wxmpapidemo.payment.dto.PayResponse;
import com.example.wxmpapidemo.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@Tag(name = "支付", description = "微信支付相关接口")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/jsapi")
    @Operation(summary = "JSAPI 下单", description = "创建订单并返回小程序调起支付所需参数")
    public ApiResponse<PayResponse> createOrder(Authentication authentication,
                                                @Valid @RequestBody CreateOrderRequest request) {
        String userId = (String) authentication.getPrincipal();
        return ApiResponse.success(paymentService.createJsapiOrder(userId, request));
    }

    @PostMapping("/notify")
    @Operation(summary = "支付回调通知", description = "接收微信支付结果通知")
    public ResponseEntity<Map<String, String>> handleNotify(
            @RequestBody String body,
            @RequestHeader("Wechatpay-Serial") String serialNumber,
            @RequestHeader("Wechatpay-Nonce") String nonce,
            @RequestHeader("Wechatpay-Timestamp") String timestamp,
            @RequestHeader("Wechatpay-Signature") String signature) {
        try {
            paymentService.handlePayNotify(body, serialNumber, nonce, timestamp, signature);
            return ResponseEntity.ok(Map.of("code", "SUCCESS", "message", "成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", "FAIL", "message", e.getMessage()));
        }
    }

    @GetMapping("/order/{orderNo}")
    @Operation(summary = "查询订单", description = "根据订单号查询订单状态")
    public ApiResponse<OrderResponse> getOrder(Authentication authentication,
                                               @PathVariable String orderNo) {
        String userId = (String) authentication.getPrincipal();
        return ApiResponse.success(paymentService.getOrder(orderNo, userId));
    }
}
