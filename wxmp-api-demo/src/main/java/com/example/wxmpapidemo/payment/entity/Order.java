package com.example.wxmpapidemo.payment.entity;

import com.example.wxmpapidemo.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "orders")
public class Order extends BaseEntity {

    @EqualsAndHashCode.Include
    @Column(name = "order_no", nullable = false, unique = true)
    private String orderNo;

    @Column(nullable = false)
    private String openid;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "transaction_id")
    private String transactionId;
}
