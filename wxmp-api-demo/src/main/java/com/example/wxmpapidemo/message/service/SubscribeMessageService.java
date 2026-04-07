package com.example.wxmpapidemo.message.service;

import com.example.wxmpapidemo.common.exception.WxApiException;
import com.example.wxmpapidemo.message.dto.SendSubscribeMessageRequest;
import com.example.wxmpapidemo.message.dto.SendMessageResponse;
import com.example.wxmpapidemo.message.entity.SubscribeMessage;
import com.example.wxmpapidemo.message.repository.SubscribeMessageRepository;
import com.example.wxmpapidemo.user.entity.User;
import com.example.wxmpapidemo.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class SubscribeMessageService {

    private static final Logger log = LoggerFactory.getLogger(SubscribeMessageService.class);
    private static final String SEND_MESSAGE_URL = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send";

    private final SubscribeMessageRepository subscribeMessageRepository;
    private final UserRepository userRepository;
    private final WxAccessTokenService accessTokenService;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public SubscribeMessageService(SubscribeMessageRepository subscribeMessageRepository,
                                   UserRepository userRepository,
                                   WxAccessTokenService accessTokenService) {
        this.subscribeMessageRepository = subscribeMessageRepository;
        this.userRepository = userRepository;
        this.accessTokenService = accessTokenService;
        this.restClient = RestClient.builder().build();
        this.objectMapper = new ObjectMapper();
    }

    public SendMessageResponse send(Long userId, SendSubscribeMessageRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new WxApiException(404, "用户不存在"));

        if (user.getOpenid() == null) {
            throw new WxApiException(400, "用户未授权，无法获取 openid");
        }

        String accessToken = accessTokenService.getAccessToken();
        String url = SEND_MESSAGE_URL + "?access_token=" + accessToken;

        Map<String, Object> body = new HashMap<>();
        body.put("touser", user.getOpenid());
        body.put("template_id", request.getTemplateId());

        if (request.getPage() != null) {
            body.put("page", request.getPage());
        }
        if (request.getMiniprogramState() != null) {
            body.put("miniprogram_state", request.getMiniprogramState());
        }
        if (request.getLang() != null) {
            body.put("lang", request.getLang());
        }

        Map<String, Object> data = buildTemplateData(request.getData());
        body.put("data", data);

        String messageId = UUID.randomUUID().toString().replace("-", "");

        SubscribeMessage message = SubscribeMessage.builder()
                .messageId(messageId)
                .openid(user.getOpenid())
                .templateId(request.getTemplateId())
                .page(request.getPage())
                .miniprogramState(request.getMiniprogramState())
                .lang(request.getLang())
                .status("PENDING")
                .build();

        try {
            message.setDataJson(objectMapper.writeValueAsString(request.getData()));
        } catch (JsonProcessingException e) {
            log.warn("序列化消息数据失败", e);
        }

        subscribeMessageRepository.save(message);

        try {
            Map<String, Object> response = restClient.post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (response == null) {
                message.setStatus("FAILED");
                message.setErrorMsg("微信返回空响应");
                subscribeMessageRepository.save(message);
                throw new WxApiException(500, "发送订阅消息失败：微信返回空响应");
            }

            Number errcode = (Number) response.get("errcode");
            String errmsg = (String) response.get("errmsg");

            if (errcode == null || errcode.intValue() == 0) {
                message.setStatus("SENT");
                subscribeMessageRepository.save(message);
                log.info("订阅消息发送成功，messageId: {}, openid: {}", messageId, user.getOpenid());
                return new SendMessageResponse(messageId, true, null);
            } else {
                message.setStatus("FAILED");
                message.setErrorMsg(errmsg);
                subscribeMessageRepository.save(message);
                log.warn("订阅消息发送失败，errcode: {}, errmsg: {}", errcode, errmsg);
                return new SendMessageResponse(messageId, false, "发送失败: " + errmsg);
            }
        } catch (RestClientException e) {
            message.setStatus("FAILED");
            message.setErrorMsg(e.getMessage());
            subscribeMessageRepository.save(message);
            log.error("调用微信发送订阅消息接口异常", e);
            throw new WxApiException(500, "发送订阅消息异常: " + e.getMessage());
        }
    }

    public SendMessageResponse sendByOpenid(String openid, SendSubscribeMessageRequest request) {
        String accessToken = accessTokenService.getAccessToken();
        String url = SEND_MESSAGE_URL + "?access_token=" + accessToken;

        Map<String, Object> body = new HashMap<>();
        body.put("touser", openid);
        body.put("template_id", request.getTemplateId());

        if (request.getPage() != null) {
            body.put("page", request.getPage());
        }
        if (request.getMiniprogramState() != null) {
            body.put("miniprogram_state", request.getMiniprogramState());
        }
        if (request.getLang() != null) {
            body.put("lang", request.getLang());
        }

        Map<String, Object> data = buildTemplateData(request.getData());
        body.put("data", data);

        String messageId = UUID.randomUUID().toString().replace("-", "");

        SubscribeMessage message = SubscribeMessage.builder()
                .messageId(messageId)
                .openid(openid)
                .templateId(request.getTemplateId())
                .page(request.getPage())
                .miniprogramState(request.getMiniprogramState())
                .lang(request.getLang())
                .status("PENDING")
                .build();

        try {
            message.setDataJson(objectMapper.writeValueAsString(request.getData()));
        } catch (JsonProcessingException e) {
            log.warn("序列化消息数据失败", e);
        }

        subscribeMessageRepository.save(message);

        try {
            Map<String, Object> response = restClient.post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (response == null) {
                message.setStatus("FAILED");
                message.setErrorMsg("微信返回空响应");
                subscribeMessageRepository.save(message);
                throw new WxApiException(500, "发送订阅消息失败：微信返回空响应");
            }

            Number errcode = (Number) response.get("errcode");
            String errmsg = (String) response.get("errmsg");

            if (errcode == null || errcode.intValue() == 0) {
                message.setStatus("SENT");
                subscribeMessageRepository.save(message);
                log.info("订阅消息发送成功，messageId: {}, openid: {}", messageId, openid);
                return new SendMessageResponse(messageId, true, null);
            } else {
                message.setStatus("FAILED");
                message.setErrorMsg(errmsg);
                subscribeMessageRepository.save(message);
                log.warn("订阅消息发送失败，errcode: {}, errmsg: {}", errcode, errmsg);
                return new SendMessageResponse(messageId, false, "发送失败: " + errmsg);
            }
        } catch (RestClientException e) {
            message.setStatus("FAILED");
            message.setErrorMsg(e.getMessage());
            subscribeMessageRepository.save(message);
            log.error("调用微信发送订阅消息接口异常", e);
            throw new WxApiException(500, "发送订阅消息异常: " + e.getMessage());
        }
    }

    private Map<String, Object> buildTemplateData(Map<String, String> data) {
        Map<String, Object> templateData = new HashMap<>();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            Map<String, String> item = new HashMap<>();
            item.put("value", entry.getValue());
            templateData.put(entry.getKey(), item);
        }
        return templateData;
    }
}
