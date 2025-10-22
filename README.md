# MyBatis-Plus Dynamic Table Starter

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/com.lizhuolun/mybatis-plus-dynamic-table-starter.svg)](https://search.maven.org/artifact/com.lizhuolun/mybatis-plus-dynamic-table-starter)
[![Java](https://img.shields.io/badge/Java-21+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6+-green.svg)](https://spring.io/projects/spring-boot)

基于MyBatis-Plus的动态表名Starter，支持多种分表策略，让分表操作变得简单高效。

## ✨ 特性

- 🚀 **开箱即用** - 零配置启动，自动装配
- 📅 **日期分表** - 支持按年、月、日进行分表
- 🔢 **哈希分表** - 支持基于哈希值的均匀分表
- 🎯 **注解驱动** - 使用注解简化分表操作
- 🛠️ **工具类支持** - 提供便捷的工具类方法
- 🔧 **灵活配置** - 支持多种配置方式
- 📝 **完整日志** - 详细的日志记录和调试信息
- 🎨 **优雅设计** - 基于策略模式，易于扩展

## 📦 安装

### Maven

```xml
<dependency>
    <groupId>com.lizhuolun</groupId>
    <artifactId>mybatis-plus-dynamic-table-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```gradle
implementation 'com.lizhuolun:mybatis-plus-dynamic-table-starter:1.0.0'
```

## 🚀 快速开始

### 1. 添加依赖

确保您的项目已包含以下依赖：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
</dependency>
<dependency>
    <groupId>com.lizhuolun</groupId>
    <artifactId>mybatis-plus-dynamic-table-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 配置分表策略

在 `application.yml` 中配置分表策略：

```yaml
# 动态表名配置
dynamic-table:
  # 启用动态表名功能
  enabled: true
  # 启用SQL日志打印
  enable-sql-log: true
  
  # 日期分表配置
  date-sharding:
    - tables: 
        - t_order
        - t_log
      date-pattern: "yyyyMM"  # 按月分表
      priority: 1
    
    - tables:
        - t_access_log
      date-pattern: "yyyyMMdd"  # 按日分表
      priority: 2
  
  # 哈希分表配置
  hash-sharding:
    - tables:
        - t_user
        - t_user_profile
      table-count: 8  # 分8张表
      priority: 10
```

### 3. 使用注解方式（推荐）

```java
@Service
public class OrderService {
    
    @Autowired
    private OrderMapper orderMapper;
    
    /**
     * 创建订单 - 自动根据当前日期分表
     */
    @DateSharding(value = "t_order", dateParam = "date")
    public Order createOrder(Order order, LocalDate date) {
        orderMapper.insert(order);
        return order;
    }
    
    /**
     * 查询订单 - 自动根据日期分表
     */
    @DateSharding(value = "t_order", dateParam = "date")
    public List<Order> getOrdersByDate(LocalDate date) {
        return orderMapper.selectList(null);
    }
}
```

### 4. 使用工具类方式

```java
@Service
public class OrderService {
    
    @Autowired
    private OrderMapper orderMapper;
    
    /**
     * 创建订单 - 使用工具类
     */
    public Order createOrder(Order order, LocalDate date) {
        return DynamicTableUtils.executeWithDate("t_order", date, () -> {
            orderMapper.insert(order);
            return order;
        });
    }
    
    /**
     * 查询订单 - 使用工具类
     */
    public List<Order> getOrdersByDate(LocalDate date) {
        return DynamicTableUtils.executeWithDate("t_order", date, () -> {
            return orderMapper.selectList(null);
        });
    }
}
```

## 📖 详细配置

### 配置参数说明

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `dynamic-table.enabled` | Boolean | true | 是否启用动态表名功能 |
| `dynamic-table.enable-sql-log` | Boolean | false | 是否启用SQL日志打印 |
| `dynamic-table.date-sharding` | List | [] | 日期分表配置列表 |
| `dynamic-table.hash-sharding` | List | [] | 哈希分表配置列表 |

### 日期分表配置

```yaml
dynamic-table:
  date-sharding:
    - tables: ["t_order", "t_log"]  # 支持的表名列表
      date-pattern: "yyyyMM"        # 日期格式：yyyy(年)、yyyyMM(月)、yyyyMMdd(日)
      priority: 1                   # 优先级，数值越小优先级越高
```

### 哈希分表配置

```yaml
dynamic-table:
  hash-sharding:
    - tables: ["t_user", "t_user_profile"]  # 支持的表名列表
      table-count: 8                        # 分表数量
      priority: 10                          # 优先级
```

## 🎯 使用示例

### 日期分表示例

```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    /**
     * 创建订单
     */
    @PostMapping
    public Result<Order> createOrder(@RequestBody OrderCreateDTO dto) {
        Order order = orderService.createOrder(dto, LocalDate.now());
        return Result.success(order);
    }
    
    /**
     * 查询指定日期的订单
     */
    @GetMapping("/{date}")
    public Result<List<Order>> getOrdersByDate(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        List<Order> orders = orderService.getOrdersByDate(date);
        return Result.success(orders);
    }
}
```

### 哈希分表示例

```java
@Service
public class UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    /**
     * 创建用户 - 根据用户名哈希分表
     */
    @HashSharding(value = "t_user", hashKey = "username")
    public User createUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        userMapper.insert(user);
        return user;
    }
    
    /**
     * 查询用户 - 根据用户名哈希分表
     */
    @HashSharding(value = "t_user", hashKey = "username")
    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }
}
```

## 🔧 高级用法

### 自定义分表策略

```java
@Component
public class CustomTableRouterStrategy implements TableRouterStrategy {
    
    @Override
    public String getActualTableName(String logicTableName, Object context) {
        // 自定义分表逻辑
        if ("t_custom".equals(logicTableName)) {
            // 根据业务规则生成实际表名
            return logicTableName + "_" + generateSuffix(context);
        }
        return logicTableName;
    }
    
    @Override
    public boolean match(String logicTableName) {
        return "t_custom".equals(logicTableName);
    }
    
    @Override
    public String getStrategyName() {
        return "CustomTableRouterStrategy";
    }
    
    @Override
    public int getPriority() {
        return 100; // 优先级
    }
}
```

### 多表操作

```java
@Service
public class MultiTableService {
    
    /**
     * 同时操作多个分表
     */
    public void batchOperation() {
        Map<String, String> tableMap = new HashMap<>();
        tableMap.put("t_order", "t_order_202501");
        tableMap.put("t_log", "t_log_202501");
        
        DynamicTableUtils.executeWithTables(tableMap, () -> {
            // 同时操作多个表
            orderMapper.insert(order);
            logMapper.insert(log);
        });
    }
}
```

## 📊 性能优化

### 1. 连接池配置

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### 2. 日志级别调整

```yaml
logging:
  level:
    com.lizhuolun.mybatis.dynamic: INFO  # 生产环境建议使用INFO级别
```

## 🐛 故障排除

### 常见问题

1. **表名未替换**
   - 检查配置是否正确
   - 确认表名是否在配置的tables列表中
   - 查看日志确认策略是否匹配

2. **分表策略不生效**
   - 检查@DateSharding或@HashSharding注解参数
   - 确认参数名或参数索引是否正确
   - 查看AOP是否正常工作

3. **SQL执行异常**
   - 检查实际表是否存在
   - 确认表结构是否一致
   - 查看SQL日志确认表名替换结果

### 调试模式

```yaml
dynamic-table:
  enable-sql-log: true  # 启用SQL日志

logging:
  level:
    com.lizhuolun.mybatis.dynamic: DEBUG  # 开启调试日志
```

## 🤝 贡献指南

我们欢迎所有形式的贡献！

1. Fork 本仓库
2. 创建您的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交您的更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开一个 Pull Request

## 📄 许可证

本项目基于 Apache License 2.0 许可证开源 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 👨‍💻 作者

**李卓伦** - [GitHub](https://github.com/lizhuolun)

## 🙏 致谢

- [MyBatis-Plus](https://github.com/baomidou/mybatis-plus) - 强大的MyBatis增强工具
- [Spring Boot](https://spring.io/projects/spring-boot) - 优秀的Java应用框架
- 所有为开源社区做出贡献的开发者们

## 📞 联系方式

- 项目地址：[https://github.com/sxhjlzl/mybatis-plus-dynamic-table-starter](https://github.com/sxhjlzl/mybatis-plus-dynamic-table-starter)
- 问题反馈：[Issues](https://github.com/sxhjlzl/mybatis-plus-dynamic-table-starter/issues)
- 邮箱：maybe.zhuo@qq.com

---

如果这个项目对您有帮助，请给我们一个 ⭐️ Star！
