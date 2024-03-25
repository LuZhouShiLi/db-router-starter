package cn.bugstack.middleware.db.router;

/**
 * 数据源基础配置
 * 主要用来获取当前线程锁指定的表索引tbIdx
 *
 * 使用线程局部变量ThreadLocal 来存储每一个线程独有的数据库表索引
 */
public class DBRouterBase {

    private String tbIdx;


    public String getTbIdx(){
        return DBContextHolder.getTBKey();// 获取当前线程中指定的表索引
    }


}
