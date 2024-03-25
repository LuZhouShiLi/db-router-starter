package cn.bugstack.middleware.db.router;


/**
 * 数据路由配置   定义一个名为DBRouterConfig的数据库路由配置
 * 分库数量 表示数据库被划分为多少个子库
 * 分表数量  表示每一个库中表的分表数量
 * routerKey路由字段 决定数据将被路由到哪一个数据库或者哪一个表中
 */
public class DBRouterConfig {

    /**
     * 分库数量
     */
    private int dbCount;

    /**
     * 分表数量
     */
    private int tbCount;

    /**
     * 路由字段
     */
    private String routerKey;

    public DBRouterConfig() {
    }

    public DBRouterConfig(int dbCount, int tbCount, String routerKey) {
        this.dbCount = dbCount;
        this.tbCount = tbCount;
        this.routerKey = routerKey;
    }


    public int getDbCount() {
        return dbCount;
    }

    public void setDbCount(int dbCount) {
        this.dbCount = dbCount;
    }

    public int getTbCount() {
        return tbCount;
    }

    public void setTbCount(int tbCount) {
        this.tbCount = tbCount;
    }

    public String getRouterKey() {
        return routerKey;
    }

    public void setRouterKey(String routerKey) {
        this.routerKey = routerKey;
    }
}
