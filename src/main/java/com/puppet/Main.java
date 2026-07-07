package com.puppet;

/**
 * 程序入口类，演示简易 MyBatis 框架的基本使用。
 * <p>
 * 通过 {@link MySqlSessionFactory} 获取 Mapper 接口的代理对象，
 * 然后调用 Mapper 方法执行数据库查询。
 * </p>
 */
public class Main {

    public static void main(String[] args) {
        // 创建会话工厂实例
        MySqlSessionFactory mySqlSessionFactory = new MySqlSessionFactory();

        // 通过动态代理获取 UserMapper 接口的实现
        UserMapper mapper = mySqlSessionFactory.getMapper(UserMapper.class);

        // 根据 ID 查询单个用户
        System.out.println(mapper.selectById(1));

        // 根据 ID 和用户名组合条件查询
        System.out.println(mapper.selectByIdAndName(12, "谭嘉伦"));
    }
}
