package com.example.wxmpapidemo.config;

import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WechatPayConfig {

    private final WxPayConfig wxPayConfig;

    public WechatPayConfig(WxPayConfig wxPayConfig) {
        this.wxPayConfig = wxPayConfig;
    }

    @Bean
    public Config wechatPaySdkConfig() {
        return new RSAAutoCertificateConfig.Builder()
                .merchantId(wxPayConfig.getMchId())
                .privateKeyFromPath(wxPayConfig.getPrivateKeyPath())
                .merchantSerialNumber(wxPayConfig.getMerchantSerialNumber())
                .apiV3Key(wxPayConfig.getApiV3Key())
                .build();
    }

    @Bean
    public JsapiServiceExtension jsapiServiceExtension(Config wechatPaySdkConfig) {
        return new JsapiServiceExtension.Builder()
                .config(wechatPaySdkConfig)
                .build();
    }
}
