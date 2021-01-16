package com.luxf.sharding;

import org.apache.shardingsphere.core.route.PreparedStatementRoutingEngine;
import org.apache.shardingsphere.core.route.router.masterslave.ShardingMasterSlaveRouter;
import org.apache.shardingsphere.core.route.router.sharding.ShardingRouter;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.statement.ShardingPreparedStatement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * sharding sphere 主从-读写分离
 * <p>
 * shard()和route()的核心入口、
 *
 * @author 小66
 * @see ShardingPreparedStatement#shard() SQL语句分库分表的入口、
 * @see PreparedStatementRoutingEngine#route(java.util.List)
 * @see ShardingMasterSlaveRouter#route(org.apache.shardingsphere.core.route.SQLRouteResult)
 * @see ShardingRouter#route(java.lang.String, java.util.List, org.apache.shardingsphere.core.parse.antlr.sql.statement.SQLStatement)
 */
@SpringBootApplication
@EnableCaching
public class ShardingReadWriteApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShardingReadWriteApplication.class, args);
    }
}