package com.puppet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义参数注解，用于指定 Mapper 接口方法参数对应的数据库列名。
 * 在运行时通过该注解获取参数与数据库字段的映射关系，
 * 从而动态构建 SQL 语句的 WHERE 条件。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Param {

    /**
     * 参数对应的数据库列名。
     *
     * @return 数据库列名字符串
     */
    String value();
}
