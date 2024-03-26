package cn.bugstack.middleware.db.router.config;

import cn.bugstack.middleware.db.router.DBRouterConfig;
import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import cn.bugstack.middleware.db.router.strategy.impl.DBRouterStrategyHashCode;
import cn.bugstack.middleware.db.router.util.PropertyUtil;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;


/**
 *  数据源配置解析
 */
public class DataSourceAutoConfig implements EnvironmentAware {

    /**
     * 分库全局属性
     */
    private static final  String TAG_GLOBAL = "global";

    /**
     * 连接池属性
     */
    private static final  String TAG_POOL = "pool";


    private Map<String, Map<String,Object>> dataSourceMap = new HashedMap();

    /**
     * 默认数据源配置
     */
    private Map<String,Object> defaultDataSourceConfig;

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


    /**
     * 初始化路由配置
     * @return
     */
    @Bean
    public DBRouterConfig dbRouterConfig(){
        return new DBRouterConfig(dbCount,tbCount,routerKey);
    }


    @Bean
    public IDBRouterStrategy dbRouterStrategy(DBRouterConfig dbRouterConfig){
        return new DBRouterStrategyHashCode(dbRouterConfig);
    }



    /**
     * 通过实现该方法 从配置文件读取数据库的相关信息  库数量 表数量  路由键  数据源列表
     * @param environment
     */
    @Override
    public void setEnvironment(Environment environment) {
        String prefix = "mini-db-router.jdbc.datasource.";

        dbCount = Integer.parseInt(Objects.requireNonNull(environment.getProperty(prefix + "dbCount")));
        tbCount = Integer.parseInt(Objects.requireNonNull(environment.getProperty(prefix + "tbCount")));

        routerKey = environment.getProperty(prefix + "routerKey");

        // 分库分表数据源
        String dataSources = environment.getProperty(prefix + "list");
        Map<String,Object> globalInfo = getGlobalProps(environment,)



    }

    /**
     * 从Spring环境变量中获取全局配置属性  包含福哦个数据源共享的配置信息
     * @param environment
     * @param key
     * @return
     */
    private Map<String,Object> getGlobalProps(Environment environment,String key){
        try{
            return PropertyUtil.handle(environment,key,Map.class);
        }catch (Exception e){
            return Collections.EMPTY_MAP;
        }
    }

}
