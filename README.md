# SimpleMyBatis

一个简易的 MyBatis 框架实现，通过 JDK 动态代理为 Mapper 接口自动生成代理实现类，演示了 MyBatis 核心原理。

## 项目结构

```
SimpleMyBatis/
├── src/main/java/com/puppet/
│   ├── Main.java                  # 程序入口，演示框架使用
│   ├── MySqlSessionFactory.java   # 核心类：会话工厂 & 动态代理处理器
│   ├── Param.java                 # 自定义注解：映射方法参数到数据库列名
│   ├── TableName.java             # 自定义注解：映射实体类到数据库表名
│   ├── User.java                  # 实体类：对应 user 表
│   └── UserMapper.java            # Mapper 接口：定义数据库操作方法
└── pom.xml                        # Maven 构建配置
```

## 核心原理

本框架的核心思想与 MyBatis 一致——**使用 JDK 动态代理在运行时为 Mapper 接口生成代理对象**。

### 工作流程

1. **获取代理对象**：`MySqlSessionFactory.getMapper()` 通过 `Proxy.newProxyInstance()` 创建 Mapper 接口的代理实例
2. **拦截方法调用**：代理内部的 `MyInvocationHandler.invoke()` 拦截所有方法调用
3. **动态生成 SQL**：根据方法名、返回类型、方法参数上的注解动态构建 SQL 语句
4. **执行数据库查询**：建立 JDBC 连接，设置参数，执行查询
5. **封装返回结果**：通过反射将 `ResultSet` 数据映射为实体对象

### SQL 生成规则

以方法 `User selectByIdAndName(@Param("id") int id, @Param("name") String name)` 为例，生成的 SQL 为：

```sql
select id,name,age from user where id = ? and name = ?
```

- **查询列**：返回类型（`User`）的所有字段名，逗号分隔
- **表名**：返回类型上的 `@TableName` 注解值
- **WHERE 条件**：方法参数上的 `@Param` 注解值，以 `AND` 连接

## 自定义注解

| 注解 | 作用 | 使用位置 |
|------|------|----------|
| `@TableName` | 指定实体类对应的数据库表名 | 实体类上 |
| `@Param` | 指定方法参数对应的数据库列名 | Mapper 方法参数上 |

## 快速开始

### 环境要求

- JDK 25+
- Maven 3+
- MySQL 8+

### 数据库准备

在 MySQL 中创建数据库和表：

```sql
CREATE DATABASE handwriting;
USE handwriting;

CREATE TABLE user (
    id   INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50),
    age  INT
);

INSERT INTO user VALUES (1, '张三', 25), (12, '谭嘉伦', 30);
```

### 运行

```bash
# 编译
mvn compile

# 运行
mvn exec:java -Dexec.mainClass="com.puppet.Main"
```

或者直接在 IDE 中运行 `Main.main()` 方法。

## 技术栈

- **Java 25** — 使用反射、动态代理、注解等核心特性
- **MySQL Connector/J 8.0.33** — MySQL 数据库驱动
- **Lombok 1.18.42** — 通过 `@Data` 注解简化实体类代码
- **Maven** — 项目构建管理
