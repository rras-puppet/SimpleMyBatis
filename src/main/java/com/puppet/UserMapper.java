package com.puppet;

/**
 * 用户数据访问接口（Mapper）。
 * 定义对 user 表的各种数据库操作方法，
 * 由 {@link MySqlSessionFactory} 通过 JDK 动态代理自动生成实现类。
 */
public interface UserMapper {

    /**
     * 根据用户ID查询单个用户。
     *
     * @param id 用户ID
     * @return 匹配的用户对象，若未找到则返回 null
     */
    User selectById(@Param("id") int id);

    /**
     * 根据用户ID和用户名查询单个用户。
     *
     * @param id   用户ID
     * @param name 用户名
     * @return 匹配的用户对象，若未找到则返回 null
     */
    User selectByIdAndName(@Param("id") int id, @Param("name") String name);
}
