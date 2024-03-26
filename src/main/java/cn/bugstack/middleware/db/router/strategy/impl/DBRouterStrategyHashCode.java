package cn.bugstack.middleware.db.router.strategy.impl;

import cn.bugstack.middleware.db.router.DBContextHolder;
import cn.bugstack.middleware.db.router.DBRouterConfig;
import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 哈希路由
 */
public class DBRouterStrategyHashCode implements IDBRouterStrategy {

    private Logger logger = LoggerFactory.getLogger(DBRouterStrategyHashCode.class);

    private DBRouterConfig dbRouterConfig;// 路由配置

    public DBRouterStrategyHashCode(DBRouterConfig dbRouterConfig) {
        this.dbRouterConfig = dbRouterConfig;
    }

    /**
     * 路由计算策略
     * @param dbKeyAttr  路由字段
     */
    @Override
    public void doRouter(String dbKeyAttr) {

        // 计算总大小  分库数量 * 分表数量
        int size = dbRouterConfig.getDbCount() * dbRouterConfig.getTbCount();

        // 扰动函数  这个函数通过将哈希值与其右移16位后的值进行异或操作，然后与(size - 1)进行与操作，得到一个索引值idx
        int idx = (size - 1)  & (dbKeyAttr.hashCode()^(dbKeyAttr.hashCode() >>> 16));

        // 计算数据库索引 idx计算数据库索引dbIdx 表索引tbIdx
        // dbIdx是通过将idx除以表数量然后加1得到的（因为索引从1开始），
        // tbIdx是通过idx减去前面数据库索引所占的表总数得到的。
        int dbIdx = idx / dbRouterConfig.getTbCount() + 1;
        int tbIdx = idx - dbRouterConfig.getTbCount() * (dbIdx - 1);

        // 设置到 ThreadLocal 关于ThreadLocal 的使用场景和源码
        // 确保每一个线程都有自己独立的路由设置 避免多线程环境下的数据干扰
        DBContextHolder.setDbKey(String.format("%02d", dbIdx));
        DBContextHolder.setTBKey(String.format("%03d", tbIdx));
        logger.debug("数据库路由 dbIdx：{} tbIdx：{}",  dbIdx, tbIdx);
    }

    @Override
    public void setDBKey(int dbIdx) {
        DBContextHolder.setDbKey(String.format("%02d",dbIdx));
    }

    @Override
    public void setTBKey(int tbIdx) {
        DBContextHolder.setTBKey(String.format("%03d",tbIdx));
    }

    @Override
    public int dbCount() {
        return dbRouterConfig.getDbCount();
    }

    @Override
    public int tbCount() {
        return dbRouterConfig.getTbCount();
    }

    @Override
    public void clear() {
        // 线程局部变量清楚上下文信息
        DBContextHolder.clearDBKey();
        DBContextHolder.clearTBKey();
    }
}
