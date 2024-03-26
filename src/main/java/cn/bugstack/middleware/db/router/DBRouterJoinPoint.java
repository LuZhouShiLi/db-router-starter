package cn.bugstack.middleware.db.router;


import cn.bugstack.middleware.db.router.annotation.DBRouter;
import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Field;


/**
 * 数据路由切面   通过自定义注解的方式拦截被切面的方法  进行数据库路由
 * 实现数据库路由逻辑 通过对标有DBRouter注解的方法进行拦截  基于方法参数的值来决定应该路由到哪一个数据库或者数据表中
 *
 * aspect 定义一个切面类 当以切点和植入的代码啊
 */
@Aspect
public class DBRouterJoinPoint {

    private Logger logger = LoggerFactory.getLogger(DBRouterJoinPoint.class);

    private DBRouterConfig dbRouterConfig;// 数据库路由配置信息

    private IDBRouterStrategy dbRouterStrategy;// 路由策略

    public DBRouterJoinPoint(DBRouterConfig dbRouterConfig, IDBRouterStrategy dbRouterStrategy) {
        this.dbRouterConfig = dbRouterConfig;
        this.dbRouterStrategy = dbRouterStrategy;
    }

    /**
     * 定义一个切点  匹配素有被DBRouter注解标记的方法  都会被拦截
     */
    @Pointcut("@annotation(cn.bugstack.middleware.db.router.annotation.DBRouter)")
    public void aopPoint(){

    }

    /**
     * 所有需要分库分表的操作 都需要使用自定义注解进行拦截  拦截之后读取方法中的入参字段  根据字段进行路由操作
     * @param jp
     * @param dbRouter
     * @return
     * @throws Throwable
     */
    @Around("aopPoint() && @annotation(dbRouter)")
    public Object doRouter(ProceedingJoinPoint jp, DBRouter dbRouter) throws Throwable{
        String dbKey = dbRouter.key();// 根据哪一个字段进行路由

        if(StringUtils.isBlank(dbKey) && StringUtils.isBlank(dbRouterConfig.getRouterKey())){
            throw new RuntimeException("annotation DBRouter key is null!");
        }

        dbKey = StringUtils.isNotBlank(dbKey) ? dbKey:dbRouterConfig.getRouterKey();

        // 从注解中获取路由键名称
        String dbKeyAttr = getAttrValue(dbKey,jp.getArgs());

        // 路由策略
        dbRouterStrategy.doRouter(dbKeyAttr);


        // 返回结构
        try{
            return jp.proceed();
        }finally {
            dbRouterStrategy.clear();
        }
    }

    /**
     * 从方法的参数中获取指定的属性名attr的值
     * 从方法的参数获取指定的属性名attr 的值
     * @param attr
     * @param args
     * @return
     */
    public String getAttrValue(String attr,Object[] args){

        // 如果参数只有一个  并且这个参数是字符串
        if (1 == args.length) {
            Object arg = args[0];
            if (arg instanceof String) {

                // 直接将字符串作为结果返回
                return arg.toString();
            }
        }

        String filedValue = null;

        // 遍历所有参数  对于每一个参数 尝试获取与指定属性名attr相对应的值
        for (Object arg : args) {
            try {
                if (StringUtils.isNotBlank(filedValue)) {
                    break;
                }
                // filedValue = BeanUtils.getProperty(arg, attr);
                // fix: 使用lombok时，uId这种字段的get方法与idea生成的get方法不同，会导致获取不到属性值，改成反射获取解决

                // 使用反射方法getValueByName获取属性值
                filedValue = String.valueOf(this.getValueByName(arg, attr));
            } catch (Exception e) {
                logger.error("获取路由属性值失败 attr：{}", attr, e);
            }
        }
        return filedValue;
    }


    /**
     * 获取对象的特定属性值
     * @param item
     * @param name
     * @return
     */
    private Object getValueByName(Object item, String name) {
        try {
            Field field = getFieldByName(item, name);
            if (field == null) {
                return null;
            }
            field.setAccessible(true);
            Object o = field.get(item);
            field.setAccessible(false);
            return o;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    /**
     * 根据名称获取方法 该方法同时兼顾继承类获取父类的属性
     * @param item
     * @param name
     * @return
     */
    private Field getFieldByName(Object item, String name) {
        try {
            Field field;
            try {
                field = item.getClass().getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                field = item.getClass().getSuperclass().getDeclaredField(name);
            }
            return field;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

}
