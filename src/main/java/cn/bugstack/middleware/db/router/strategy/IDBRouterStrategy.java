package cn.bugstack.middleware.db.router.strategy;

/**
 * 路由策略
 */
public interface IDBRouterStrategy {

    /**
     * 路由计算
     * @param dbKeyAttr  路由字段
     */
    void doRouter(String dbKeyAttr);

    /**
     * 手动设置分库路由
     * @param dbIdx  路由库 需要在配置范围内
     */
    void setDBKey(int dbIdx);

    /**
     * 手动设置分表路由
     * @param tbIdx
     */
    void setTBKey(int tbIdx);

    /**
     * 获取分库数
     * @return
     */
    int dbCount();

    /**
     * 获取分表数
     * @return
     */
    int tbCount();

    /**
     * 清楚路由
     */
    void clear();
}



