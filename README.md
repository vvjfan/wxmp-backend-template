# WXMP API Template

微信小程序 API 模板项目，基于 Spring Boot 3 构建，提供小程序开发常用功能的脚手架。

## 功能特性

- **微信登录** — 小程序 code2session 登录，JWT 认证
- **微信支付** — 统一下单、支付回调
- **订阅消息** — 模板消息推送
- **人脸核身** — 微信人脸核身接口集成
- **用户管理** — 用户资料 CRUD
- **API 文档** — SpringDoc OpenAPI (Swagger UI)

## 技术栈

- Java 17+
- Spring Boot 3.x
- Spring Security + JWT
- Spring Data JPA
- H2 Database (开发环境)
- SpringDoc OpenAPI
- 微信支付 SDK

## 快速开始

### 前置条件

- JDK 17 或更高版本
- Maven 3.6+
- (可选) Redis

### 配置

复制环境变量配置并填写你的微信小程序/支付参数：

```bash
export WX_MINIAPP_APP_ID=your-app-id
export WX_MINIAPP_APP_SECRET=your-app-secret
export WX_PAY_APP_ID=your-app-id
export WX_PAY_MCH_ID=your-mch-id
export WX_PAY_API_V3_KEY=your-api-v3-key
export WX_PAY_PRIVATE_KEY_PATH=/path/to/apiclient_key.pem
export WX_PAY_MERCHANT_SERIAL_NUMBER=your-merchant-serial-number
export JWT_SECRET=your-256-bit-secret-key-here-must-be-at-least-32-chars
```

或在 `application.yaml` 中直接修改默认值。

### 运行

```bash
cd wxmp-api-demo
mvn spring-boot:run
```

启动后访问：

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- H2 Console: `http://localhost:8080/h2-console`

## 项目结构

```
wxmp-api-demo/
├── src/main/java/com/example/wxmpapidemo/
│   ├── auth/          # 认证登录 (JWT, 微信登录)
│   ├── common/        # 通用组件 (异常处理, 响应封装)
│   ├── config/        # 配置类 (Security, JWT, 微信)
│   ├── face/          # 人脸核身
│   ├── message/       # 订阅消息
│   ├── payment/       # 微信支付
│   └── user/          # 用户管理
└── src/main/resources/
    └── application.yaml
```

## API 接口

| 模块 | 路径前缀 | 说明 |
|------|----------|------|
| Auth | `/api/auth` | 登录、刷新 Token、绑定手机 |
| User | `/api/user` | 用户资料查询与更新 |
| Payment | `/api/payment` | 下单、支付回调 |
| Message | `/api/message` | 订阅消息发送 |
| Face | `/api/face` | 人脸核身 |

## License

MIT License — 详见 [LICENSE](LICENSE) 文件。
