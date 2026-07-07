package com.puppet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义表名注解，用于标注实体类对应的数据库表名。
 * 在动态生成 SQL 时，通过该注解获取要查询的目标表名称。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TableName {

    /**
     * 数据库表名。
     *
     * @return 表名字符串
     */
    String tableName();
}
