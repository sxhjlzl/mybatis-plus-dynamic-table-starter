# 贡献指南

感谢您对 MyBatis-Plus Dynamic Table Starter 项目的关注！我们欢迎所有形式的贡献。

## 🤝 如何贡献

### 报告问题

如果您发现了bug或有功能建议，请通过以下方式报告：

1. **搜索现有问题** - 在 [Issues](https://github.com/lizhuolun/mybatis-plus-dynamic-table/issues) 中搜索是否已有类似问题
2. **创建新问题** - 如果没有找到相关问题，请创建新的Issue
3. **提供详细信息** - 包括问题描述、复现步骤、环境信息等

### 提交代码

1. **Fork 项目** - 点击项目页面的 Fork 按钮
2. **克隆仓库** - 克隆您fork的仓库到本地
3. **创建分支** - 创建新的功能分支
4. **编写代码** - 实现您的功能或修复
5. **编写测试** - 为您的代码编写测试用例
6. **提交代码** - 提交您的更改
7. **创建PR** - 创建Pull Request

## 📋 开发规范

### 代码规范

- **Java代码**：遵循阿里巴巴Java开发手册
- **注释规范**：使用中文注释，格式规范
- **命名规范**：使用有意义的变量和方法名
- **异常处理**：完善的异常处理和日志记录

### 提交规范

使用以下格式提交代码：

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Type类型**：
- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 构建过程或辅助工具的变动

**示例**：
```
feat(interceptor): 添加动态表名拦截器

- 实现基于MyBatis-Plus的InnerInterceptor
- 支持SQL表名动态替换
- 添加详细的日志记录

Closes #123
```

### 测试要求

- **单元测试**：为新功能编写单元测试
- **集成测试**：确保与现有功能的兼容性
- **测试覆盖率**：保持较高的测试覆盖率
- **性能测试**：对性能敏感的功能进行性能测试

## 🛠️ 开发环境

### 环境要求

- **JDK**: 21+
- **Maven**: 3.6+
- **IDE**: IntelliJ IDEA 或 Eclipse
- **数据库**: MySQL 8.0+ 或 H2（测试用）

### 项目结构

```
mybatis-plus-dynamic-table-starter/
├── src/main/java/com/lizhuolun/mybatis/dynamic/
│   ├── annotation/          # 注解定义
│   ├── aspect/             # AOP切面
│   ├── config/             # 配置类
│   ├── context/            # 上下文管理
│   ├── interceptor/        # 拦截器
│   ├── strategy/           # 策略实现
│   └── util/               # 工具类
├── src/main/resources/
│   └── META-INF/           # Spring Boot自动配置
├── src/test/               # 测试代码
├── pom.xml                 # Maven配置
├── README.md               # 项目说明
├── LICENSE                 # 开源协议
└── CHANGELOG.md            # 更新日志
```

### 本地开发

1. **克隆项目**
   ```bash
   git clone https://github.com/lizhuolun/mybatis-plus-dynamic-table.git
   cd mybatis-plus-dynamic-table/mybatis-plus-dynamic-table-starter
   ```

2. **安装依赖**
   ```bash
   mvn clean install
   ```

3. **运行测试**
   ```bash
   mvn test
   ```

4. **构建项目**
   ```bash
   mvn clean package
   ```

## 📝 文档贡献

### 文档类型

- **API文档**：代码注释和JavaDoc
- **使用文档**：README.md和示例代码
- **配置文档**：配置参数说明
- **故障排除**：常见问题解决方案

### 文档规范

- **语言**：使用中文编写
- **格式**：使用Markdown格式
- **示例**：提供完整可运行的示例
- **更新**：及时更新相关文档

## 🐛 Bug修复

### 修复流程

1. **确认问题** - 复现并确认bug
2. **分析原因** - 分析bug的根本原因
3. **编写修复** - 实现修复方案
4. **编写测试** - 编写测试用例防止回归
5. **提交PR** - 提交修复代码

### 修复要求

- **最小化影响** - 修复应该最小化对现有功能的影响
- **向后兼容** - 保持API的向后兼容性
- **测试覆盖** - 确保修复被测试覆盖
- **文档更新** - 更新相关文档

## ✨ 功能开发

### 新功能流程

1. **讨论设计** - 在Issue中讨论功能设计
2. **编写代码** - 实现新功能
3. **编写测试** - 编写完整的测试用例
4. **更新文档** - 更新相关文档
5. **提交PR** - 提交功能代码

### 功能要求

- **设计合理** - 功能设计应该合理且易于使用
- **性能良好** - 新功能不应该显著影响性能
- **向后兼容** - 保持与现有功能的兼容性
- **文档完整** - 提供完整的使用文档

## 🔍 代码审查

### 审查要点

- **代码质量** - 代码是否清晰、可读
- **功能正确** - 功能是否按预期工作
- **测试覆盖** - 是否有足够的测试覆盖
- **性能影响** - 是否对性能有负面影响
- **安全性** - 是否存在安全漏洞

### 审查流程

1. **自动检查** - CI/CD自动检查
2. **人工审查** - 维护者进行代码审查
3. **测试验证** - 验证功能正确性
4. **合并代码** - 审查通过后合并

## 📞 联系方式

- **GitHub Issues**: [项目Issues](https://github.com/lizhuolun/mybatis-plus-dynamic-table/issues)
- **邮箱**: your-email@example.com
- **讨论区**: [GitHub Discussions](https://github.com/lizhuolun/mybatis-plus-dynamic-table/discussions)

## 🙏 致谢

感谢所有为项目做出贡献的开发者！您的贡献让项目变得更好。

---

**注意**: 请确保您的贡献符合项目的开源协议（Apache License 2.0）。
