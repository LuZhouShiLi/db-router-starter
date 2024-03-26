package cn.bugstack.middleware.db.router.config;

import cn.bugstack.middleware.db.router.DBRouterConfig;
import cn.bugstack.middleware.db.router.DBRouterJoinPoint;
import cn.bugstack.middleware.db.router.dynamic.DynamicDataSource;
import cn.bugstack.middleware.db.router.dynamic.DynamicMybatisPlugin;
import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import cn.bugstack.middleware.db.router.strategy.impl.DBRouterStrategyHashCode;
import cn.bugstack.middleware.db.router.util.PropertyUtil;
import cn.bugstack.middleware.db.router.util.StringUtils;
import com.sun.org.apache.bcel.internal.generic.DASTORE;
import org.apache.commons.collections.map.HashedMap;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.*;


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


    private Map<String, Map<String,Object>> dataSourceMap = new HashMap<>();

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
     * 创建数据库路由的连接点  DBRouterJoinPoint
     * @param dbRouterConfig
     * @param idbRouterStrategy
     * @return
     */
    @Bean(name = "db-router-point")
    @ConditionalOnMissingBean
    public DBRouterJoinPoint point(DBRouterConfig dbRouterConfig,IDBRouterStrategy idbRouterStrategy){
        return new DBRouterJoinPoint(dbRouterConfig,idbRouterStrategy);
    }


    /**
     * 初始化路由配置
     * @return
     */
    @Bean
    public DBRouterConfig dbRouterConfig(){
        return new DBRouterConfig(dbCount,tbCount,routerKey);
    }



    /**
     * 定义一个Mybatis插件 用于实现动态数据源的切换
     * @return
     */
    @Bean
    public Interceptor plugin(){
        return new DynamicMybatisPlugin();
    }

    /**
     * 根据传入的属性 创建一个数据源
     * @param attributes
     * @return
     */
    private DataSource createDataSource(Map<String,Object> attributes){
        try{
            DataSourceProperties dataSourceProperties = new DataSourceProperties();
            dataSourceProperties.setUrl(attributes.get("url").toString());
            dataSourceProperties.setUsername(attributes.get("username").toString());
            dataSourceProperties.setPassword(attributes.get("password").toString());

            String driverClassName = attributes.get("driver-class-name") == null ? "com.zaxxer.hikari.HikariDataSource" : attributes.get("driver-class-name").toString();
            dataSourceProperties.setDriverClassName(driverClassName);

            String typeClassName = attributes.get("type-class-name") == null ? "com.zaxxer.hikari.HikariDataSource" : attributes.get("type-class-name").toString();
            DataSource ds = dataSourceProperties.initializeDataSourceBuilder().type((Class<DataSource>) Class.forName(typeClassName)).build();

            MetaObject dsMeta = SystemMetaObject.forObject(ds);
            Map<String, Object> poolProps = (Map<String, Object>) (attributes.containsKey(TAG_POOL) ? attributes.get(TAG_POOL) : Collections.EMPTY_MAP);
            for (Map.Entry<String, Object> entry : poolProps.entrySet()) {
                // 中划线转驼峰
                String key = StringUtils.middleScoreToCamelCase(entry.getKey());
                if (dsMeta.hasSetter(key)) {
                    dsMeta.setValue(key, entry.getValue());
                }
            }
            return ds;

        }catch(ClassNotFoundException e){
            throw new IllegalArgumentException("can not find datasource type class by class name",e);
        }
    }


    @Bean
    public DataSource createDataSource(){
        // 创建数据源
        Map<Object,Object> targetDataSources = new HashMap<>();

        for(String dbInfo:dataSourceMap.keySet()){
            Map<String,Object> objMap =  dataSourceMap.get(dbInfo);

            DataSource ds = createDataSource(objMap);
            targetDataSources.put(dbInfo,ds);
        }

        // 设置数据源
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.setTargetDataSources(targetDataSources);
        dynamicDataSource.setDefaultTargetDataSource(createDataSource(defaultDataSourceConfig));

        return dynamicDataSource;

    }

    @Bean
    public IDBRouterStrategy dbRouterStrategy(DBRouterConfig dbRouterConfig){
        return new DBRouterStrategyHashCode(dbRouterConfig);
    }

    /**
     * 配置和创建一个TransactionTemplate 实例  提供的一个编程事务管理工具  用于管理针对JDBC数据源的事务
     * @param dataSource
     * @return
     */
    @Bean
    public TransactionTemplate transactionTemplate(DataSource dataSource){
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(dataSource);
        TransactionTemplate transactionTemplate = new TransactionTemplate();
        transactionTemplate.setTransactionManager(dataSourceTransactionManager);
        transactionTemplate.setPropagationBehaviorName("PROPAGATION_REQUIRED");
        return transactionTemplate;
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
        Map<String, Object> globalInfo = getGlobalProps(environment, prefix + TAG_GLOBAL);
        for (String dbInfo : dataSources.split(",")) {
            final String dbPrefix = prefix + dbInfo;
            Map<String, Object> dataSourceProps = PropertyUtil.handle(environment, dbPrefix, Map.class);
            injectGlobal(dataSourceProps, globalInfo);
            dataSourceMap.put(dbInfo, dataSourceProps);
        }

        // 默认数据源
        String defaultData = environment.getProperty(prefix + "default");
        defaultDataSourceConfig = PropertyUtil.handle(environment, prefix + defaultData, Map.class);
        injectGlobal(defaultDataSourceConfig, globalInfo);


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

    private void injectGlobal(Map<String, Object> origin, Map<String, Object> global) {
        for (String key : global.keySet()) {
            if (!origin.containsKey(key)) {
                origin.put(key, global.get(key));
            } else if (origin.get(key) instanceof Map) {
                injectGlobal((Map<String, Object>) origin.get(key), (Map<String, Object>) global.get(key));
            }
        }
    }

}
