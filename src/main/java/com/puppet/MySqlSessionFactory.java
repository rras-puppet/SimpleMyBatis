package com.puppet;

import java.lang.reflect.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 简易 MyBatis 会话工厂类。
 * <p>
 * 核心功能：通过 JDK 动态代理为 Mapper 接口生成代理实现类，
 * 在调用代理方法时动态解析方法签名、生成 SQL 语句、执行数据库查询并封装返回结果。
 * </p>
 */
public class MySqlSessionFactory {

    /** 数据库连接 URL */
    private static final String JDBCURL = "jdbc:mysql://localhost:3306/handwriting";

    /** 数据库用户名 */
    private static final String DBUSER = "root";

    /** 数据库密码 */
    private static final String PASSWORD = "123456";

    /**
     * 获取 Mapper 接口的代理实现对象。
     *
     * @param mapper Mapper 接口的 Class 对象
     * @param <T>    Mapper 接口类型
     * @return Mapper 接口的代理实例
     */
    @SuppressWarnings("all")
    public <T> T getMapper(Class<T> mapper) {
        return (T) Proxy.newProxyInstance(
                MySqlSessionFactory.class.getClassLoader(),
                new Class[]{mapper},
                new MyInvocationHandler()
        );
    }

    /**
     * 自定义 InvocationHandler，拦截 Mapper 接口的方法调用。
     * 根据方法名前缀（如 "select"）路由到对应的 SQL 处理器。
     */
    private static class MyInvocationHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            // 根据方法名前缀判断操作类型，目前仅支持 select 查询
            if (methodName.startsWith("select")) {
                return invokeSelect(proxy, method, args);
            }
            return null;
        }

        /**
         * 处理 select 查询请求：动态生成 SQL、设置参数、执行查询并解析结果。
         *
         * @param proxy  代理对象
         * @param method 被调用的方法
         * @param args   方法参数
         * @return 查询结果对象
         */
        private Object invokeSelect(Object proxy, Method method, Object[] args) {
            // 1. 动态生成 SQL 语句
            String selectSql = createSelectSql(method);
            System.out.println("[SQL] " + selectSql);

            // 2. 建立数据库连接并执行查询
            try (Connection conn = DriverManager.getConnection(JDBCURL, DBUSER, PASSWORD);
                 PreparedStatement statement = conn.prepareStatement(selectSql)) {

                // 3. 设置 SQL 参数（从方法参数中获取）
                for (int i = 0; i < args.length; i++) {
                    Object arg = args[i];
                    if (arg instanceof Integer) {
                        statement.setInt(i + 1, (Integer) arg);
                    } else if (arg instanceof String) {
                        statement.setString(i + 1, arg.toString());
                    }
                }

                // 4. 执行查询
                ResultSet rs = statement.executeQuery();

                // 5. 解析结果集并返回实体对象
                if (rs.next()) {
                    return parseResult(rs, method.getReturnType());
                }
            } catch (Exception e) {
                // 异常静默处理（生产环境应记录日志）
                e.printStackTrace();
            }
            return null;
        }

        /**
         * 将 ResultSet 中的当前行数据映射为指定类型的实体对象。
         * 通过反射创建实体实例，并遍历所有字段进行赋值。
         *
         * @param rs         SQL 查询结果集
         * @param returnType 返回实体类型的 Class 对象
         * @return 映射后的实体对象
         * @throws Exception 反射操作或类型转换异常
         */
        private Object parseResult(ResultSet rs, Class<?> returnType) throws Exception {
            // 通过无参构造器创建实体对象
            Constructor<?> constructor = returnType.getConstructor();
            Object result = constructor.newInstance();

            // 遍历实体类的所有字段，从 ResultSet 中取同名列的值并注入
            Field[] declaredFields = returnType.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                declaredField.setAccessible(true);
                declaredField.set(result, rs.getObject(declaredField.getName()));
            }

            return result;
        }

        /**
         * 根据 Mapper 方法签名动态生成 SELECT 查询 SQL。
         * <p>
         * 生成规则：
         * <ol>
         *   <li>查询列为返回类型的所有字段名（逗号分隔）</li>
         *   <li>表名通过返回类型上的 {@link TableName} 注解获取</li>
         *   <li>WHERE 条件通过方法参数上的 {@link Param} 注解获取（以 AND 连接）</li>
         * </ol>
         *
         * @param method Mapper 接口方法
         * @return 生成的 SQL 字符串
         * @throws RuntimeException 如果返回类型未标注 {@link TableName} 注解
         */
        private String createSelectSql(Method method) {
            StringBuilder sql = new StringBuilder();
            sql.append("select ");

            // 获取返回类型的 Class 对象
            Class<?> returnType = method.getReturnType();
            Field[] fields = returnType.getDeclaredFields();

            // 构建 SELECT 子句：取实体所有字段名，以逗号分隔
            String columns = Arrays.stream(fields)
                    .map(Field::getName)
                    .collect(Collectors.joining(","));
            sql.append(columns);

            sql.append(" from ");

            // 获取表名注解
            TableName annotation = returnType.getAnnotation(TableName.class);
            if (annotation == null) {
                throw new RuntimeException("实体类 " + returnType.getSimpleName() + " 缺少 @TableName 注解，无法确定表名");
            }
            sql.append(annotation.tableName());

            // 构建 WHERE 子句：根据方法参数的 @Param 注解生成条件
            sql.append(" where ");
            String whereClause = Arrays.stream(method.getParameters())
                    .map(param -> {
                        Param paramAnnotation = param.getAnnotation(Param.class);
                        String columnName = paramAnnotation.value();
                        return columnName + " = ?";
                    })
                    .collect(Collectors.joining(" and "));
            sql.append(whereClause);

            return sql.toString();
        }
    }
}
