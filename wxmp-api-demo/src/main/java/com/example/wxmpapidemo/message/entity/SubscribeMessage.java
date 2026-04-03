package com.example.wxmpapidemo.message.entity;

import com.example.wxmpapidemo.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Table(name = "subscribe_messages")
public class SubscribeMessage extends BaseEntity {

    @EqualsAndHashCode.Include
    @Column(name = "message_id", nullable = false, unique = true)
    private String messageId;

    @Column(name = "openid", nullable = false)
    private String openid;

    @Column(name = "template_id", nullable = false)
    private String templateId;

    @Column(name = "page")
    private String page;

    @Column(name = "miniprogram_state")
    private String miniprogramState;

    @Column(name = "lang")
    private String lang;

    @Column(name = "data_json", columnDefinition = "TEXT")
    private String dataJson;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "error_msg")
    private String errorMsg;
}
