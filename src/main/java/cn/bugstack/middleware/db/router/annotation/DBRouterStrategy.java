package cn.bugstack.middleware.db.router.annotation;


import java.lang.annotation.*;

/**
 * 路由策略  控制数据库操作的分表策略
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DBRouterStrategy {

    boolean splitTable() default false;
}
