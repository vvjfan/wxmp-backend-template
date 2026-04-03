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
@Table(name = "message_templates")
public class MessageTemplate extends BaseEntity {

    @EqualsAndHashCode.Include
    @Column(name = "template_id", nullable = false)
    private String templateId;

    @Column(name = "template_name", nullable = false)
    private String templateName;

    @Column(name = "template_content")
    private String templateContent;

    @Column(name = "category")
    private String category;

    @Column(name = "example")
    private String example;
}
