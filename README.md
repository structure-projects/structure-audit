# Structure Audit Center

审计中心是一个用于记录和管理系统操作日志的服务平台，支持实时审计、历史查询、数据迁移等功能。

## 功能特性

### 1. 审计消息处理
- **请求审计**：通过 RabbitMQ 接收请求消息，仅存储到 Redis 缓存
- **响应审计**：接收响应消息后，从 Redis 读取对应的请求数据进行数据合并，然后存储到数据库
- **数据合并策略**：
  - 请求数据优先级：操作人信息、操作行为、操作模块、客户端信息（IP、UserAgent）
  - 响应数据优先级：操作结果、操作状态、错误信息、耗时

### 2. 实时审计查询
- 支持按操作行为、模块、状态、时间范围等条件查询
- 使用 MyBatis Plus 实现高效分页查询
- 支持关键词全文搜索

### 3. 历史审计查询
- 30天前的审计记录自动迁移到 Elasticsearch
- 支持按多种条件组合查询历史记录
- 提供高效的全文本搜索能力

### 4. 自动数据清理
- 定时任务（XXL-Job）自动清理30天前的审计记录
- 清理前自动将数据备份到 Elasticsearch
- 确保数据安全性和系统性能

### 5. AOP 审计注解
- 提供 `@AuditLog` 注解用于方法级审计
- 自动捕获方法参数、执行结果、耗时等信息
- 支持自定义操作行为和模块名称

### 6. 灵活存储策略
- 支持三种存储策略：仅MySQL、仅Elasticsearch、同时存储
- 可在配置文件中灵活配置默认存储策略
- 基于配置实现存储策略切换，无需修改代码
- 支持高并发场景，直接写入ES获得更好的性能

## 技术架构

### 模块结构
```
structure-audit
├── structure-audit-api              # API 接口定义模块
│   ├── aop/                        # AOP 注解定义（@AuditLog）
│   ├── constant/                   # 常量定义（RabbitMQ 相关常量）
│   ├── dto/                       # 数据传输对象（AuditDTO）
│   ├── service/                   # 远程服务接口（Feign 接口）
│   └── vo/                        # 视图对象
├── structure-audit-domain          # 领域层模块
│   ├── assembler/                 # 对象转换器
│   ├── controller/                # 控制器
│   ├── entity/                   # 实体类
│   ├── mapper/                   # MyBatis Mapper
│   └── service/                  # 领域服务
├── structure-audit-biz            # 业务逻辑层模块
│   ├── aspect/                   # 切面类
│   ├── configureation/           # 配置类
│   ├── controller/              # 控制器
│   ├── entity/                  # ES 实体
│   ├── event/                   # 消息事件处理
│   ├── repository/              # ES 仓储
│   ├── service/                # 业务服务
│   └── task/                   # 定时任务
├── structure-audit-boot          # 启动模块
├── structure-audit-dependencies  # 依赖管理模块
└── structure-audit-client-example # 客户端接入示例模块
    ├── aspect/                  # 审计切面实现
    ├── config/                  # Web 配置
    ├── controller/             # 示例控制器
    ├── feign/                  # Feign 客户端
    ├── interceptor/            # 租户上下文拦截器
    └── service/                # 审计服务实现
```

### 技术栈
| 分类 | 技术 |
|------|------|
| 基础框架 | Spring Boot 2.7.18 |
| 安全框架 | Spring Security 5.7 |
| 数据库 | MySQL 8.0 + MyBatis Plus |
| 缓存 | Redis |
| 消息队列 | RabbitMQ |
| 搜索引擎 | Elasticsearch |
| 定时任务 | XXL-Job |
| 数据库迁移 | Flyway |

## 快速开始

### 环境要求
- JDK 8+
- Maven 3.9+
- MySQL 8.0+
- Redis 7+
- RabbitMQ 3.x
- Elasticsearch 7.x
- XXL-Job 2.x

### 配置说明

在 `application.yml` 中配置以下服务连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://${HOST}:3306/audit?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8
    username: root
    password: ${PASSWORD}

  redis:
    host: ${HOST}
    port: 6379
    password: ${PASSWORD}

  elasticsearch:
    rest:
      uris: http://${HOST}:9200
      username: elastic
      password: ${ES_PASSWORD}

  rabbitmq:
    host: ${HOST}
    port: 5672
    username: root
    password: ${PASSWORD}
```

### 构建部署

1. **编译项目**
```bash
cd structure-audit-dependencies
mvn clean install -DskipTests

cd ../structure-audit-api
mvn clean install -DskipTests

cd ../structure-audit-domain
mvn clean install -DskipTests

cd ../structure-audit-biz
mvn clean install -DskipTests

cd ../structure-audit-boot
mvn clean package -DskipTests
```

2. **启动服务**
```bash
java -jar target/audit-center.jar
```

## 示例工程使用

`structure-audit-client-example` 模块提供了完整的三种接入方式示例，建议在实际接入前先运行示例工程进行测试。

### 快速开始

#### 1. 编译主项目（先安装依赖到本地仓库）

```bash
cd structure-audit
mvn clean install -DskipTests
```

#### 2. 修改配置

编辑 `structure-audit-client-example/src/main/resources/application.yml`，配置审计服务地址和 RabbitMQ 连接：

```yaml
server:
  port: 8081

spring:
  application:
    name: audit-client-example

  rabbitmq:
    host: 172.24.20.15
    port: 5672
    username: root
    password: 123456

audit:
  server:
    url: http://audit-center
```

#### 3. 编译运行

```bash
cd structure-audit-client-example
mvn clean compile
mvn spring-boot:run
```

#### 4. 测试接口

服务启动后，可通过以下接口测试三种审计方式：

##### 方式一：MQ消息方式

```bash
# 用户登录
curl -X POST http://localhost:8081/api/example/mq/login \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: 4" \
  -d '{"username":"admin","password":"123456"}'

# 数据导出
curl -X POST http://localhost:8081/api/example/mq/export \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: 4" \
  -d '{"type":"excel","startTime":"2026-01-01","endTime":"2026-05-31"}'
```

##### 方式二：HTTP远程调用方式

```bash
# 创建订单
curl -X POST http://localhost:8081/api/example/http/order \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: 4" \
  -d '{"productId":"P001","quantity":10,"address":"北京市朝阳区"}'

# 删除用户
curl -X DELETE http://localhost:8081/api/example/http/user/user-1001 \
  -H "X-Tenant-Id: 4"
```

##### 方式三：注解方式

```bash
# 用户注册（操作人信息通过请求头传递）
curl -X POST http://localhost:8081/api/example/annotation/register \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: 4" \
  -H "X-Operator-Id: user-003" \
  -H "X-Operator-Name: 王五" \
  -d '{"username":"newuser","password":"123456","email":"test@example.com"}'

# 修改密码
curl -X POST http://localhost:8081/api/example/annotation/change-password \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: 4" \
  -H "X-Operator-Id: user-001" \
  -H "X-Operator-Name: 张三" \
  -d '{"oldPassword":"123456","newPassword":"654321"}'
```

### 验证租户透传

测试完成后，可以通过审计中心的查询接口验证租户ID是否正确透传：

```bash
# 查询实时审计记录（需要先启动审计中心）
curl -X GET "http://audit-center/api/audit/list?currentPage=1&pageSize=10" \
  -H "Content-Type: application/json"
```

## API 接口

### 审计查询接口

#### 查询实时审计记录
```
GET /api/audit/list
```

**请求参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| currentPage | Integer | 否 | 当前页码，默认1 |
| pageSize | Integer | 否 | 每页条数，默认10 |
| action | String | 否 | 操作行为 |
| module | String | 否 | 操作模块 |
| status | Integer | 否 | 操作状态（1成功 0失败） |
| startTime | LocalDateTime | 否 | 开始时间 |
| endTime | LocalDateTime | 否 | 结束时间 |
| keyword | String | 否 | 关键词搜索 |

**响应示例：**
```json
{
  "code": 200,
  "data": {
    "total": 100,
    "currentPage": 1,
    "pageSize": 10,
    "list": [
      {
        "id": 1,
        "requestId": "req-123456",
        "operationId": "user-001",
        "operationAlias": "张三",
        "action": "登录",
        "module": "用户模块",
        "operationParams": "{\"username\":\"admin\"}",
        "operationResult": "{\"code\":200}",
        "status": 1,
        "ipAddress": "192.168.1.100",
        "userAgent": "Mozilla/5.0...",
        "costTime": 150,
        "createTime": "2026-05-19 10:00:00"
      }
    ]
  }
}
```

#### 查询历史审计记录
```
GET /api/audit/history/list
```

**请求参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| action | String | 否 | 操作行为 |
| module | String | 否 | 操作模块 |
| status | Integer | 否 | 操作状态 |
| startTime | LocalDateTime | 否 | 开始时间 |
| endTime | LocalDateTime | 否 | 结束时间 |

### 手动添加审计记录
```
POST /open-api/audit/add
```

**请求参数：**
```json
{
  "requestId": "req-123456",
  "operatorId": "user-001",
  "operatorName": "张三",
  "action": "登录",
  "module": "用户模块",
  "operationParams": "{\"username\":\"admin\"}",
  "operationResult": "{\"code\":200}",
  "status": 1,
  "ipAddress": "192.168.1.100",
  "userAgent": "Mozilla/5.0...",
  "costTime": 150
}
```

## 租户上下文集成

审计系统已集成租户上下文支持，审计消息会自动携带租户ID（organizationId），实现多租户数据隔离。

### 请求头说明

| 请求头名称 | 说明 | 示例值 |
|------------|------|--------|
| `X-Tenant-Id` | 租户ID | `4` |
| `X-Operator-Id` | 操作人ID | `user-001` |
| `X-Operator-Name` | 操作人名称 | `张三` |

### 客户端发送消息

在发送审计消息时，系统会自动从 `TenantContextHolder` 获取当前租户ID：

```java
// 示例：获取租户ID
private Long getOrganizationId() {
    try {
        String tenantId = TenantContextHolder.getTenantId();
        if (tenantId != null && !tenantId.isEmpty()) {
            return Long.parseLong(tenantId);
        }
    } catch (Exception e) {
        log.warn("获取租户ID失败: {}", e.getMessage());
    }
    return 1L;  // 默认租户ID
}

// 发送请求消息时自动注入租户ID
public String sendRequestMessage(...) {
    AuditDTO auditDTO = new AuditDTO();
    auditDTO.setOrganizationId(getOrganizationId());  // 自动获取租户ID
    // ...
}
```

### 服务端接收消息

审计服务在接收消息后会自动设置租户上下文：

```java
// 示例：消息监听器中的实现
@RabbitListener(queues = AuditRabbitConstants.AUDIT_REQUEST_QUEUE)
public void onRequestMessage(AuditDTO auditDTO) {
    try {
        // 设置租户上下文
        if (auditDTO.getOrganizationId() != null) {
            TenantContextHolder.setTenantId(String.valueOf(auditDTO.getOrganizationId()));
        }
        // 处理业务逻辑
        auditBizService.handleRequest(auditDTO);
    } finally {
        TenantContextHolder.clear();  // 清理租户上下文
    }
}
```

### 数据合并

请求和响应消息合并时，租户ID的优先级为：
1. 请求消息中的 organizationId（优先）
2. 响应消息中的 organizationId


## RabbitMQ 配置

### 交换机和队列定义

| 类型 | 名称 | 说明 |
|------|------|------|
| Exchange | exchange.audit | 审计消息交换机 |
| Queue | queue.audit.request | 请求消息队列 |
| Queue | queue.audit.response | 响应消息队列 |
| Routing Key | audit.request | 请求消息路由键 |
| Routing Key | audit.response | 响应消息路由键 |

### 消息格式

```json
{
  "requestId": "唯一请求ID",
  "operatorId": "操作人ID",
  "operatorName": "操作人名称",
  "action": "操作行为",
  "module": "操作模块",
  "operationParams": "操作参数(JSON)",
  "operationResult": "操作结果(JSON)",
  "status": 1,
  "errorMsg": "错误信息",
  "ipAddress": "操作IP",
  "userAgent": "用户代理",
  "costTime": 100
}
```

## 客户端接入方式

审计中心提供三种接入方式，适用于不同的业务场景：

### 三种调用方式对比

| 方式 | 适用场景 | 优点 | 缺点 |
|------|----------|------|------|
| **MQ消息方式** | 需要记录请求和响应的完整链路 | 异步解耦，性能好，支持请求-响应分离 | 需要配置 RabbitMQ |
| **HTTP远程调用** | 简单的单条审计记录 | 实现简单，无需MQ | 同步调用，有网络开销 |
| **注解方式** | 方法级自动审计 | 侵入性低，代码简洁 | 需要AOP支持 |

### 方式一：MQ消息方式（详细流程）

#### 1. 引入依赖

在您的业务项目中添加审计相关的依赖：

```xml
<dependency>
    <groupId>cn.structured</groupId>
    <artifactId>structure-audit-api</artifactId>
    <version>1.0.1-SNAPSHOT</version>
</dependency>
```

**注意**：如果 `structure-audit-api` 未发布到私服，您可以直接复制 `AuditDTO` 和 `AuditRabbitConstants` 类到您的项目中。

#### 2. 配置 RabbitMQ 连接

在 `application.yml` 中添加审计消息队列的连接配置：

```yaml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:root}
    password: ${RABBITMQ_PASSWORD:root}
    virtual-host: ${RABBITMQ_VHOST:/}
```

#### 3. 注入 RabbitTemplate

```java
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuditMessageSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;
}
```

#### 4. 发送审计消息（标准流程）

##### 步骤一：发送请求消息

在业务方法开始时，生成唯一的 `requestId`，发送请求消息到审计系统：

```java
import cn.structured.audit.api.dto.AuditDTO;
import cn.structured.audit.api.constant.AuditRabbitConstants;
import com.alibaba.fastjson.JSON;

public class UserService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void login(LoginRequest request) {
        long startTime = System.currentTimeMillis();

        // 生成唯一请求ID（建议使用UUID）
        String requestId = UUID.randomUUID().toString();

        // 构建请求消息
        AuditDTO requestDTO = new AuditDTO();
        requestDTO.setRequestId(requestId);
        requestDTO.setOperatorId(getCurrentUserId());      // 操作人ID
        requestDTO.setOperatorName(getCurrentUserName()); // 操作人名称
        requestDTO.setAction("用户登录");                   // 操作行为
        requestDTO.setModule("用户模块");                   // 操作模块
        requestDTO.setOperationParams(JSON.toJSONString(request)); // 操作参数
        requestDTO.setIpAddress(getClientIpAddress());    // 客户端IP
        requestDTO.setUserAgent(getUserAgent());           // 用户代理

        // 发送请求消息到审计系统
        rabbitTemplate.convertAndSend(
            AuditRabbitConstants.AUDIT_EXCHANGE,           // 交换机名称
            AuditRabbitConstants.AUDIT_REQUEST_ROUTING_KEY, // 路由键
            requestDTO                                     // 消息内容
        );

        System.out.println("请求审计消息已发送, requestId: " + requestId);

        // 执行业务逻辑
        try {
            // ... 业务处理代码 ...

            // 步骤二：发送成功响应消息
            sendSuccessResponse(requestId, startTime);

        } catch (Exception e) {
            // 步骤二：发送失败响应消息
            sendFailResponse(requestId, e, startTime);
            throw e;
        }
    }
}
```

##### 步骤二：发送响应消息

业务处理完成后，发送响应消息到审计系统：

```java
/**
 * 发送成功响应消息
 */
private void sendSuccessResponse(String requestId, long startTime) {
    AuditDTO responseDTO = new AuditDTO();
    responseDTO.setRequestId(requestId);
    responseDTO.setStatus(1);                               // 1=成功
    responseDTO.setOperationResult("{\"code\":200,\"message\":\"登录成功\"}");
    responseDTO.setCostTime(System.currentTimeMillis() - startTime);

    rabbitTemplate.convertAndSend(
        AuditRabbitConstants.AUDIT_EXCHANGE,
        AuditRabbitConstants.AUDIT_RESPONSE_ROUTING_KEY,
        responseDTO
    );

    System.out.println("成功响应审计消息已发送, requestId: " + requestId);
}

/**
 * 发送失败响应消息
 */
private void sendFailResponse(String requestId, Exception e, long startTime) {
    AuditDTO responseDTO = new AuditDTO();
    responseDTO.setRequestId(requestId);
    responseDTO.setStatus(0);                                // 0=失败
    responseDTO.setErrorMsg(e.getMessage());                // 错误信息
    responseDTO.setCostTime(System.currentTimeMillis() - startTime);

    rabbitTemplate.convertAndSend(
        AuditRabbitConstants.AUDIT_EXCHANGE,
        AuditRabbitConstants.AUDIT_RESPONSE_ROUTING_KEY,
        responseDTO
    );

    System.out.println("失败响应审计消息已发送, requestId: " + requestId);
}
```

#### 5. 简化版本（仅发送成功消息）

如果只需要记录成功场景，可以使用简化版本：

```java
public void exportData(ExportRequest request) {
    long startTime = System.currentTimeMillis();
    String requestId = UUID.randomUUID().toString();

    // 发送请求消息
    AuditDTO requestDTO = AuditDTO.builder()
        .requestId(requestId)
        .operatorId(getCurrentUserId())
        .operatorName(getCurrentUserName())
        .action("数据导出")
        .module("报表模块")
        .operationParams(JSON.toJSONString(request))
        .ipAddress(getClientIpAddress())
        .build();

    rabbitTemplate.convertAndSend(
        AuditRabbitConstants.AUDIT_EXCHANGE,
        AuditRabbitConstants.AUDIT_REQUEST_ROUTING_KEY,
        requestDTO
    );

    try {
        // 业务处理
        doExport(request);

        // 发送成功响应
        rabbitTemplate.convertAndSend(
            AuditRabbitConstants.AUDIT_EXCHANGE,
            AuditRabbitConstants.AUDIT_RESPONSE_ROUTING_KEY,
            AuditDTO.builder()
                .requestId(requestId)
                .status(1)
                .operationResult("导出成功，共100条数据")
                .costTime(System.currentTimeMillis() - startTime)
                .build()
        );
    } catch (Exception e) {
        // 发送失败响应
        rabbitTemplate.convertAndSend(
            AuditRabbitConstants.AUDIT_EXCHANGE,
            AuditRabbitConstants.AUDIT_RESPONSE_ROUTING_KEY,
            AuditDTO.builder()
                .requestId(requestId)
                .status(0)
                .errorMsg("导出失败：" + e.getMessage())
                .costTime(System.currentTimeMillis() - startTime)
                .build()
        );
        throw e;
    }
}
```

### 方式二：HTTP远程调用方式

#### 1. 引入依赖

```xml
<dependency>
    <groupId>cn.structured</groupId>
    <artifactId>structure-audit-api</artifactId>
    <version>1.0.1-SNAPSHOT</version>
</dependency>
```

#### 2. 配置审计服务地址

```yaml
audit:
  server:
    url: http://audit-center:8080
```

#### 3. 使用示例

```java
import cn.structured.audit.api.service.IRemoteAuditService;
import cn.structured.audit.api.dto.AuditDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    @Autowired
    private IRemoteAuditService auditService;

    public void createOrder(OrderRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 执行业务逻辑
            // ...
            
            // 记录审计日志（直接调用接口）
            AuditDTO auditDTO = AuditDTO.builder()
                .requestId(UUID.randomUUID().toString())
                .operatorId(getCurrentUserId())
                .operatorName(getCurrentUserName())
                .action("创建订单")
                .module("订单模块")
                .operationParams(JSON.toJSONString(request))
                .operationResult("{\"code\":200,\"orderId\":\"ORD-123456\"}")
                .status(1)
                .ipAddress(getClientIpAddress())
                .costTime(System.currentTimeMillis() - startTime)
                .build();
            
            auditService.addAudit(auditDTO);
            
        } catch (Exception e) {
            // 记录失败审计
            auditService.addAudit(AuditDTO.builder()
                .requestId(UUID.randomUUID().toString())
                .operatorId(getCurrentUserId())
                .operatorName(getCurrentUserName())
                .action("创建订单")
                .module("订单模块")
                .status(0)
                .errorMsg(e.getMessage())
                .costTime(System.currentTimeMillis() - startTime)
                .build());
            throw e;
        }
    }
}
```

### 方式三：注解方式

#### 1. 引入依赖

```xml
<dependency>
    <groupId>cn.structured</groupId>
    <artifactId>structure-audit-api</artifactId>
    <version>1.0.1-SNAPSHOT</version>
</dependency>
```

#### 2. 使用 @AuditLog 注解

```java
import cn.structured.audit.api.aop.AuditLog;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @AuditLog(value = "用户登录", module = "用户模块")
    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginRequest request) {
        // 业务逻辑
        return Result.success("登录成功");
    }

    @AuditLog(value = "用户注册", module = "用户模块")
    @PostMapping("/register")
    public Result<String> register(@RequestBody RegisterRequest request) {
        // 业务逻辑
        return Result.success("注册成功");
    }

    @AuditLog(value = "修改密码", module = "用户模块")
    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestBody PasswordRequest request) {
        // 业务逻辑
        return Result.success();
    }
}
```

#### 3. 配置租户上下文（可选）

如果使用注解方式，需要配置租户上下文拦截器以自动获取操作人信息：

```java
@Component
public class TenantContextInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tenantId = request.getHeader("X-Tenant-Id");
        String operatorId = request.getHeader("X-Operator-Id");
        String operatorName = request.getHeader("X-Operator-Name");
        
        if (tenantId != null) {
            TenantContextHolder.setTenantId(tenantId);
        }
        if (operatorId != null) {
            TenantContextHolder.setOperatorId(operatorId);
        }
        if (operatorName != null) {
            TenantContextHolder.setOperatorName(operatorName);
        }
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContextHolder.clear();
    }
}
```

### 消息字段说明

| 字段名 | 必填 | 说明 |
|--------|------|------|
| requestId | ✅ | 唯一请求ID，用于关联请求和响应消息 |
| operatorId | ❌ | 操作人ID |
| operatorName | ❌ | 操作人名称 |
| action | ❌ | 操作行为描述，如"用户登录"、"数据删除" |
| module | ❌ | 操作所属模块，如"用户模块"、"订单模块" |
| operationParams | ❌ | 操作参数，建议使用JSON格式 |
| operationResult | ❌ | 操作结果，建议使用JSON格式 |
| status | ✅ | 操作状态：1=成功，0=失败 |
| errorMsg | ❌ | 错误信息（失败时填写） |
| ipAddress | ❌ | 客户端IP地址 |
| userAgent | ❌ | 用户代理字符串 |
| costTime | ❌ | 耗时，单位毫秒 |

### 7. 常见问题

**Q: 请求消息和响应消息都必须发送吗？**
> A: 是的。审计系统采用请求-响应分离的模式：
> - 请求消息：存储到 Redis 缓存
> - 响应消息：从 Redis 读取请求数据并与响应数据合并后，存储到数据库
> - 如果只发请求消息，数据会一直留在 Redis 中不会被存储到数据库

**Q: 如何确保消息发送成功？**
> A: 建议在业务逻辑中使用 try-catch 包裹消息发送代码，发送失败时记录日志但不阻塞业务

**Q: 响应消息中哪些字段是必须的？**
> A: `requestId` 和 `status` 是必须的，其他字段可省略（会被请求消息中的对应字段填充）

**Q: 三种接入方式应该如何选择？**
> A: 根据业务场景选择：
> - **MQ消息方式**：适合需要完整请求-响应链路追踪的场景，如用户登录、数据操作等
> - **HTTP远程调用**：适合简单的单条审计记录，如后台管理操作、系统事件等
> - **注解方式**：适合方法级自动审计，如REST API的通用记录场景

**Q: 注解方式如何获取操作人信息？**
> A: 注解方式通过租户上下文拦截器自动获取操作人信息：
> - 从请求头 `X-Operator-Id` 获取操作人ID
> - 从请求头 `X-Operator-Name` 获取操作人名称
> - 如果未设置，会自动使用默认租户ID（1）和匿名用户

**Q: HTTP远程调用方式需要配置RabbitMQ吗？**
> A: 不需要。HTTP远程调用方式通过Feign直接调用审计服务的REST API，只需要配置审计服务地址即可。

**Q: MQ方式必须发送请求和响应两条消息吗？**
> A: 是的。审计系统采用请求-响应分离模式，只发请求消息数据不会持久化到数据库。只发响应消息会导致数据不完整。

**Q: 租户隔离是如何实现的？**
> A: 系统通过 `organizationId` 字段实现租户隔离：
> - 每个租户的审计数据相互独立
> - 租户ID通过 `TenantContextHolder` 自动获取
> - 可以在消息中显式传递租户ID，或通过请求头自动透传

**Q: 注解方式采用同步还是异步记录？**
> A: 注解方式采用异步记录审计日志，不会影响业务接口的响应时间。

**Q: 所有审计方式的异常处理策略是什么？**
> A: 所有审计方式都有异常捕获机制：
> - 审计失败不会影响主业务逻辑
> - 建议在业务逻辑中使用 try-catch 包裹审计代码
> - 审计失败时记录日志便于排查问题

## 定时任务

### 数据清理与备份

**任务名称：** `cleanAndBackupOldData`

**执行策略：** 每天执行一次（建议在业务低峰期执行）

**功能说明：**
1. 查询30天前的审计记录
2. 将记录批量写入 Elasticsearch
3. 从 MySQL 中删除已备份的记录

## 数据库设计

### 审计表 (audit)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键ID |
| request_id | VARCHAR(64) | 请求ID |
| operation_id | VARCHAR(64) | 操作人ID |
| operation_alias | VARCHAR(128) | 操作人名称 |
| action | VARCHAR(64) | 操作行为 |
| module | VARCHAR(64) | 操作模块 |
| operation_params | TEXT | 操作参数 |
| operation_result | TEXT | 操作结果 |
| status | TINYINT | 操作状态（1成功 0失败） |
| error_msg | TEXT | 错误信息 |
| ip_address | VARCHAR(64) | 操作IP |
| user_agent | VARCHAR(512) | 用户代理 |
| cost_time | BIGINT | 耗时(毫秒) |
| organization_id | BIGINT | 租户ID |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 修改时间 |
| create_by | BIGINT | 创建人 |
| update_by | BIGINT | 修改人 |
| is_deleted | TINYINT | 逻辑删除 |

### 索引设计

| 索引名 | 字段 | 说明 |
|--------|------|------|
| idx_request_id | request_id | 请求ID索引 |
| idx_operation_id | operation_id | 操作人索引 |
| idx_module | module | 模块索引 |
| idx_create_time | create_time | 时间索引 |
| idx_organization_id | organization_id | 租户索引 |

## 使用示例

### 方式一：使用 AOP 注解

```java
@RestController
@RequestMapping("/user")
public class UserController {

    @AuditLog(value = "用户登录", module = "用户模块")
    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginRequest request) {
        // 业务逻辑
        return Result.success("登录成功");
    }
}
```

### 方式二：发送 RabbitMQ 消息

参考上方 **"发送审计消息（客户端接入指南）"** 章节

### 方式三：直接调用接口

```bash
curl -X POST http://localhost:8080/open-api/audit/add \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "req-123456",
    "operatorId": "user-001",
    "operatorName": "张三",
    "action": "数据导出",
    "module": "报表模块",
    "operationParams": "{\"type\":\"excel\"}",
    "status": 1,
    "ipAddress": "192.168.1.100",
    "costTime": 5000
  }'
```

## 依赖模块说明

客户端项目可以通过 Maven 依赖方式复用主项目的模块，避免代码重复：

| 复用模块 | 引入的类 | 说明 |
|----------|----------|------|
| `structure-audit-api` | `AuditDTO`, `AuditRabbitConstants`, `AuditLog` | 提供消息对象、常量和注解定义 |
| `structure-audit-domain` | `AuditAssembler` | 提供领域对象转换器 |

### Maven 依赖配置

```xml
<dependencies>
    <!-- API 模块 -->
    <dependency>
        <groupId>cn.structured</groupId>
        <artifactId>structure-audit-api</artifactId>
        <version>1.0.1-SNAPSHOT</version>
    </dependency>
    
    <!-- Domain 模块（可选） -->
    <dependency>
        <groupId>cn.structured</groupId>
        <artifactId>structure-audit-domain</artifactId>
        <version>1.0.1-SNAPSHOT</version>
    </dependency>
</dependencies>
```

**注意**：如果主项目的模块未发布到私服，您可以：
1. 将需要的类（如 `AuditDTO`、`AuditRabbitConstants`、`AuditLog`）直接复制到客户端项目中
2. 或将主项目安装到本地 Maven 仓库（`mvn clean install -DskipTests`）

## 注意事项

1. **Redis 连接**：请求消息仅存储在 Redis 中，确保 Redis 服务正常运行
2. **RabbitMQ 配置**：交换机和队列绑定关系在 `AuditRabbitConfig` 中集中定义
3. **数据合并**：响应消息处理时会自动合并 Redis 中的请求数据
4. **ES 备份**：定时任务执行前请确保 Elasticsearch 服务可用
5. **租户支持**：系统支持多租户，租户ID通过 `organization_id` 字段区分
6. **存储策略**：
   - 默认使用 MYSQL 存储策略，可在配置中修改为 ELASTICSEARCH 或 BOTH
   - 使用 ELASTICSEARCH 策略时，所有审计数据直接写入ES，不写MySQL
   - 使用 BOTH 策略时，会同时写入MySQL和ES，实现数据冗余
   - 存储策略的切换无需修改代码，只需修改配置文件即可

## 许可证

本项目基于 Apache License 2.0 许可证开源。
