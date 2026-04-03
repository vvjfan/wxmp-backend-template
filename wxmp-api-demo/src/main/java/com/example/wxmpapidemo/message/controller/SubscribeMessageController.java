package com.example.wxmpapidemo.message.controller;

import com.example.wxmpapidemo.common.dto.ApiResponse;
import com.example.wxmpapidemo.message.dto.SendByOpenidRequest;
import com.example.wxmpapidemo.message.dto.SendMessageResponse;
import com.example.wxmpapidemo.message.dto.SendSubscribeMessageRequest;
import com.example.wxmpapidemo.message.service.SubscribeMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/message")
@Tag(name = "订阅消息", description = "微信小程序订阅消息相关接口")
public class SubscribeMessageController {

    private final SubscribeMessageService subscribeMessageService;

    public SubscribeMessageController(SubscribeMessageService subscribeMessageService) {
        this.subscribeMessageService = subscribeMessageService;
    }

    @PostMapping("/subscribe")
    @Operation(summary = "发送订阅消息", description = "向当前登录用户发送订阅消息")
    public ApiResponse<SendMessageResponse> send(Authentication authentication,
                                                  @Valid @RequestBody SendSubscribeMessageRequest request) {
        String userId = (String) authentication.getPrincipal();
        return ApiResponse.success(subscribeMessageService.send(Long.parseLong(userId), request));
    }

    @PostMapping("/subscribe/by-openid")
    @Operation(summary = "通过 openid 发送订阅消息", description = "通过 openid 向指定用户发送订阅消息（管理员接口）")
    public ApiResponse<SendMessageResponse> sendByOpenid(@Valid @RequestBody SendByOpenidRequest request) {
        SendSubscribeMessageRequest messageRequest = new SendSubscribeMessageRequest();
        messageRequest.setTemplateId(request.getTemplateId());
        messageRequest.setPage(request.getPage());
        messageRequest.setMiniprogramState(request.getMiniprogramState());
        messageRequest.setLang(request.getLang());
        messageRequest.setData(request.getData());

        return ApiResponse.success(subscribeMessageService.sendByOpenid(request.getOpenid(), messageRequest));
    }
}
