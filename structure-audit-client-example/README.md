# 审计客户端示例工程

本示例工程演示如何调用结构审计中心（structure-audit）的三种方式：

1. **MQ消息方式** - 通过 RabbitMQ 发送请求/响应消息
2. **HTTP远程调用方式** - 通过 Feign 调用审计接口
3. **注解方式** - 使用 `@AuditLog` 注解自动记录审计日志

## 项目结构

```
structure-audit-client-example/
├── src/main/java/cn/structured/example/audit/client/
│   ├── AuditClientExampleApplication.java    # 启动类
│   ├── aspect/
│   │   └── AuditLogAspect.java               # 审计切面（注解方式）
│   ├── controller/
│   │   └── AuditExampleController.java       # 示例控制器
│   ├── feign/
│   │   └── AuditFeignClient.java             # Feign客户端（HTTP方式）
│   └── service/
│       └── AuditService.java                 # 审计服务（MQ方式）
├── src/main/resources/
│   └── application.yml                       # 配置文件
├── pom.xml                                   # Maven配置（复用api/domain模块）
└── README.md                                 # 使用说明
```

## 依赖复用说明

本示例工程通过 Maven 依赖引入主项目的模块，避免代码重复：

| 复用模块 | 引入的类 |
|----------|----------|
| `structure-audit-api` | `AuditDTO`, `AuditRabbitConstants`, `AuditLog` |
| `structure-audit-domain` | `AuditAssembler` 等 |

## 三种调用方式对比

| 方式 | 适用场景 | 优点 | 缺点 |
|------|----------|------|------|
| **MQ消息方式** | 需要记录请求和响应的完整链路 | 异步解耦，性能好，支持请求-响应分离 | 需要配置 RabbitMQ |
| **HTTP远程调用** | 简单的单条审计记录 | 实现简单，无需MQ | 同步调用，有网络开销 |
| **注解方式** | 方法级自动审计 | 侵入性低，代码简洁 | 需要AOP支持 |

## 快速开始

### 1. 编译主项目（先安装依赖到本地仓库）

```bash
cd structure-audit
mvn clean install -DskipTests
```

### 2. 修改配置

编辑 `application.yml`，配置审计服务地址和 RabbitMQ 连接：

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

### 3. 编译运行

```bash
cd structure-audit-client-example
mvn clean compile
mvn spring-boot:run
```

### 4. 测试接口

服务启动后，可通过以下接口测试三种审计方式：

#### 方式一：MQ消息方式

```bash
# 用户登录
curl -X POST http://localhost:8081/api/example/mq/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'

# 数据导出
curl -X POST http://localhost:8081/api/example/mq/export \
  -H "Content-Type: application/json" \
  -d '{"type":"excel","startTime":"2026-01-01","endTime":"2026-05-31"}'
```

#### 方式二：HTTP远程调用方式

```bash
# 创建订单
curl -X POST http://localhost:8081/api/example/http/order \
  -H "Content-Type: application/json" \
  -d '{"productId":"P001","quantity":10,"address":"北京市朝阳区"}'

# 删除用户
curl -X DELETE http://localhost:8081/api/example/http/user/user-1001
```

#### 方式三：注解方式

```bash
# 用户注册（操作人信息通过请求头传递）
curl -X POST http://localhost:8081/api/example/annotation/register \
  -H "Content-Type: application/json" \
  -H "X-Operator-Id: user-003" \
  -H "X-Operator-Name: 王五" \
  -d '{"username":"newuser","password":"123456","email":"test@example.com"}'

# 修改密码
curl -X POST http://localhost:8081/api/example/annotation/change-password \
  -H "Content-Type: application/json" \
  -H "X-Operator-Id: user-001" \
  -H "X-Operator-Name: 张三" \
  -d '{"oldPassword":"123456","newPassword":"654321"}'
```

## 使用说明

### 1. MQ消息方式使用步骤

```java
@Autowired
private AuditService auditService;

public void doBusiness() {
    long startTime = System.currentTimeMillis();
    
    // Step 1: 业务开始时发送请求消息
    String requestId = auditService.sendRequestMessage(
            "操作名称",
            "模块名称",
            "操作人ID",
            "操作人名称",
            参数对象,
            httpRequest
    );
    
    try {
        // Step 2: 执行业务逻辑
        // ...
        
        // Step 3: 业务成功时发送成功响应
        auditService.sendSuccessResponse(requestId, 结果对象, startTime);
    } catch (Exception e) {
        // Step 3: 业务失败时发送失败响应
        auditService.sendFailResponse(requestId, e.getMessage(), startTime);
        throw e;
    }
}
```

### 2. HTTP远程调用方式使用步骤

```java
@Autowired
private AuditService auditService;

public void doBusiness() {
    long startTime = System.currentTimeMillis();
    
    try {
        // 执行业务逻辑
        // ...
        
        // 直接记录审计日志
        auditService.addAuditByHttp(
                "操作名称",
                "模块名称",
                "操作人ID",
                "操作人名称",
                参数对象,
                结果对象,
                1,  // 1=成功
                null,
                "客户端IP",
                System.currentTimeMillis() - startTime
        );
    } catch (Exception e) {
        auditService.addAuditByHttp(
                "操作名称",
                "模块名称",
                "操作人ID",
                "操作人名称",
                参数对象,
                null,
                0,  // 0=失败
                e.getMessage(),
                "客户端IP",
                System.currentTimeMillis() - startTime
        );
        throw e;
    }
}
```

### 3. 注解方式使用步骤

```java
@RestController
@RequestMapping("/api")
public class MyController {

    @AuditLog(value = "操作名称", module = "模块名称")
    @PostMapping("/action")
    public Result<?> doAction(@RequestBody Request request) {
        // 业务逻辑
        return Result.success();
    }
}
```

## 注意事项

1. **MQ方式必须发送请求和响应两条消息**：审计系统采用请求-响应分离模式，只发请求消息数据不会持久化到数据库
2. **操作人信息获取**：注解方式从请求头 `X-Operator-Id` 和 `X-Operator-Name` 获取操作人信息
3. **异步记录**：注解方式采用异步记录审计日志，不影响业务性能
4. **异常处理**：所有审计方式都有异常捕获，记录失败不会影响主业务

## 技术栈

- Spring Boot 2.7.18
- Spring AMQP (RabbitMQ)
- Spring Cloud OpenFeign
- Spring AOP
- Lombok
- FastJSON
