package com.puppet;

import lombok.Data;

/**
 * 用户实体类，对应数据库中的 user 表。
 * 使用 {@link TableName} 注解指定表名，
 * 使用 Lombok {@link Data} 注解自动生成 getter/setter/toString 等方法。
 */
@Data
@TableName(tableName = "user")
public class User {

    /** 用户ID，对应数据库 id 字段 */
    private Integer id;

    /** 用户名，对应数据库 name 字段 */
    private String name;

    /** 用户年龄，对应数据库 age 字段 */
    private Integer age;
}
