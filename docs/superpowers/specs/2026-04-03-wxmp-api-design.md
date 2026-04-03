# 微信小程序后端 API 设计文档

## 概述

为微信小程序提供登录认证和支付功能的后端 API，包含完整的 OpenAPI 文档。

## 架构

分层单体架构：controller → service → repository。按业务模块分包。

## 技术栈

- **框架**: Spring Boot 4.0.5 (Java 17)
- **数据库**: H2 内存数据库 + JPA
- **缓存**: Redis (存储 refresh token)
- **安全**: Spring Security + JWT (HMAC-SHA256)
- **文档**: springdoc-openapi 3.0.2
- **工具**: Lombok

## Token 方案

- **Access Token**: JWT，30 分钟过期，通过 Authorization Header 传递
- **Refresh Token**: 存储在 Redis，7 天过期，key 为 `refresh:{userId}`

## API 接口

### 认证模块

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/login | 小程序登录，接收 code，返回 accessToken + refreshToken |
| POST | /api/auth/refresh | 刷新 token，接收 refreshToken，返回新 accessToken |
| POST | /api/auth/logout | 登出，使当前 token 失效 |
| POST | /api/auth/phone | 绑定手机号，接收微信 code，调用微信接口获取手机号 |

### 用户模块

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/user/profile | 获取当前用户信息 |
| PUT | /api/user/profile | 更新用户信息（昵称、头像） |

### 支付模块

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/payment/jsapi | JSAPI 下单，返回调起支付所需参数 |
| POST | /api/payment/notify | 微信支付回调通知 |
| GET | /api/payment/order/{orderNo} | 查询订单状态 |

## 数据模型

### User

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long (PK, 自增) | 主键 |
| openid | String (唯一) | 微信 openid |
| sessionKey | String | 微信 session_key |
| phoneNumber | String (唯一) | 手机号 |
| nickname | String | 用户昵称 |
| avatarUrl | String | 头像 URL |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

### Order

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long (PK, 自增) | 主键 |
| orderNo | String (唯一) | 商户订单号 |
| openid | String | 用户 openid |
| amount | Integer | 订单金额（分） |
| description | String | 订单描述 |
| status | Enum | PENDING / PAID / CLOSED |
| transactionId | String | 微信支付单号 |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

## 安全设计

- JWT 使用 HMAC-SHA256 签名，密钥通过配置注入
- 登出时将 refresh token 加入 Redis 黑名单
- 微信支付回调验签 + 解密
- 除登录和支付回调外的所有接口需要 JWT 认证
