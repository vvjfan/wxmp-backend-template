package com.example.wxmpapidemo.payment.service;

import com.example.wxmpapidemo.common.exception.WxApiException;
import com.example.wxmpapidemo.config.WxPayConfig;
import com.example.wxmpapidemo.payment.dto.CreateOrderRequest;
import com.example.wxmpapidemo.payment.dto.OrderResponse;
import com.example.wxmpapidemo.payment.dto.PayResponse;
import com.example.wxmpapidemo.payment.dto.PayResponse.PayParams;
import com.example.wxmpapidemo.payment.entity.Order;
import com.example.wxmpapidemo.payment.entity.OrderStatus;
import com.example.wxmpapidemo.payment.repository.OrderRepository;
import com.example.wxmpapidemo.user.entity.User;
import com.example.wxmpapidemo.user.repository.UserRepository;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.core.exception.ValidationException;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RSAPublicKeyNotificationConfig;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final WxPayConfig wxPayConfig;
    private final JsapiServiceExtension jsapiService;
    private final Config wechatPaySdkConfig;

    public PaymentService(OrderRepository orderRepository,
                          UserRepository userRepository,
                          WxPayConfig wxPayConfig,
                          JsapiServiceExtension jsapiService,
                          Config wechatPaySdkConfig) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.wxPayConfig = wxPayConfig;
        this.jsapiService = jsapiService;
        this.wechatPaySdkConfig = wechatPaySdkConfig;
    }

    public PayResponse createJsapiOrder(String userId, CreateOrderRequest request) {
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new WxApiException(404, "用户不存在"));

        if (user.getOpenid() == null) {
            throw new WxApiException(400, "用户未登录");
        }

        String orderNo = generateOrderNo();

        Order order = Order.builder()
                .orderNo(orderNo)
                .openid(user.getOpenid())
                .amount(request.getAmount())
                .description(request.getDescription())
                .status(OrderStatus.PENDING)
                .build();

        orderRepository.save(order);

        try {
            PrepayRequest prepayRequest = new PrepayRequest();
            prepayRequest.setAppid(wxPayConfig.getAppId());
            prepayRequest.setMchid(wxPayConfig.getMchId());
            prepayRequest.setDescription(order.getDescription());
            prepayRequest.setOutTradeNo(order.getOrderNo());
            prepayRequest.setNotifyUrl(wxPayConfig.getNotifyUrl());

            Amount amount = new Amount();
            amount.setTotal(order.getAmount());
            amount.setCurrency("CNY");
            prepayRequest.setAmount(amount);

            Payer payer = new Payer();
            payer.setOpenid(user.getOpenid());
            prepayRequest.setPayer(payer);

            PrepayWithRequestPaymentResponse response = jsapiService.prepayWithRequestPayment(prepayRequest);

            PayParams payParams = new PayParams(
                    response.getAppId(),
                    response.getTimeStamp(),
                    response.getNonceStr(),
                    response.getPackageVal(),
                    response.getSignType(),
                    response.getPaySign()
            );

            return new PayResponse(orderNo, payParams);
        } catch (ServiceException e) {
            log.error("微信下单失败: code={}, message={}", e.getErrorCode(), e.getErrorMessage());
            throw new WxApiException(500, "微信下单失败: " + e.getErrorMessage());
        } catch (Exception e) {
            log.error("微信下单异常", e);
            throw new WxApiException(500, "微信下单失败");
        }
    }

    public void handlePayNotify(String body, String serialNumber, String nonce,
                                String timestamp, String signature) {
        NotificationConfig config = new RSAPublicKeyNotificationConfig.Builder()
                .apiV3Key(wxPayConfig.getApiV3Key())
                .build();

        NotificationParser parser = new NotificationParser(config);

        RequestParam requestParam = new RequestParam.Builder()
                .serialNumber(serialNumber)
                .nonce(nonce)
                .timestamp(timestamp)
                .signature(signature)
                .body(body)
                .build();

        try {
            com.wechat.pay.java.service.payments.model.Transaction transaction =
                    parser.parse(requestParam, com.wechat.pay.java.service.payments.model.Transaction.class);

            String orderNo = transaction.getOutTradeNo();
            String transactionId = transaction.getTransactionId();

            Order order = orderRepository.findByOrderNo(orderNo)
                    .orElseThrow(() -> new WxApiException(404, "订单不存在"));

            if (order.getStatus() == OrderStatus.PAID) {
                log.info("订单 {} 已支付，忽略重复回调", orderNo);
                return;
            }

            order.setStatus(OrderStatus.PAID);
            order.setTransactionId(transactionId);
            orderRepository.save(order);

            log.info("订单 {} 支付成功，微信交易号: {}", orderNo, transactionId);
        } catch (ValidationException e) {
            log.error("支付回调验签失败", e);
            throw new WxApiException(401, "签名验证失败");
        }
    }

    public OrderResponse getOrder(String orderNo, String userId) {
        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new WxApiException(404, "订单不存在"));

        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new WxApiException(404, "用户不存在"));

        if (!order.getOpenid().equals(user.getOpenid())) {
            throw new WxApiException(403, "无权查看此订单");
        }

        return new OrderResponse(
                order.getOrderNo(),
                order.getAmount(),
                order.getDescription(),
                order.getStatus(),
                order.getTransactionId()
        );
    }

    private String generateOrderNo() {
        return System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8);
    }
}
