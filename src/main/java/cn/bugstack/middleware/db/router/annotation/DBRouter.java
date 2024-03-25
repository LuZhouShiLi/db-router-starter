package cn.bugstack.middleware.db.router.annotation;


import java.lang.annotation.*;

/**
 * 数据库路由的自定义注解
 * key 用来指定分库分表的字段  默认空字符串  如果不设置 表示进行分库分表操作
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD})
public @interface DBRouter {

    String key() default "";

}
