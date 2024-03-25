package cn.bugstack.middleware.db.router;

/**
 * 数据源上下文  管理数据库上下文信息  保证并发场景下 每一个线程可以安全访问自己专属的数据库表的索引
 * 负责在当前上下文保存和管理数据库db和tb的键值信息  用于动态数据源路由的实现
 * 该类主要为每一个线程独立存储数据库键值信息  保证多线程环境下的线程安全  每一个线程只能范围跟自己的数据库键和表键 不受其他线程干扰
 */
public class DBContextHolder {
    // 当前线程上下文中保存和管理数据库键值信息

    private static final ThreadLocal<String> dbKey = new ThreadLocal<>();
    private static final ThreadLocal<String> tbKey = new ThreadLocal<>();

    // 设置当前线程的数据库键和表键信息
    public static  void setDbKey(String dbKeyIdx){
        dbKey.set(dbKeyIdx);
    }

    /**
     *
     * @return
     */
    public static  String getDbKey(){
        return dbKey.get();
    }


    public static void setTBKey(String tbKeyIdx){
        tbKey.set(tbKeyIdx);
    }

    public static String getTBKey(){
        return tbKey.get();
    }




    public static void clearDBKey(){
        dbKey.remove();
    }

    public static void clearTBKey(){
        tbKey.remove();
    }



}
