package com.luxf.sharding.algorithm;

import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingValue;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.util.CollectionUtils;

import java.text.DateFormat;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author 小66
 * @Description
 * @create 2021-01-21 23:12
 **/
public class IComplexKeysShardingAlgorithm implements ComplexKeysShardingAlgorithm<String> {

    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, ComplexKeysShardingValue<String> shardingValue) {
        System.out.println("availableTargetNames = " + availableTargetNames);
        System.out.println("shardingValue = " + shardingValue);

//        log.info("自定义按照日期进行分表");
//        List<String> shardingSuffix = new ArrayList<>();
//        // 获取分表字段及字段值
//        Map<String, Collection<LocalDateTime>> map = shardingValue.getColumnNameAndShardingValuesMap();
//        // 获取字段值
//        Collection<LocalDateTime> shardingValues = map.get("created_time");
//        if (!CollectionUtils.isEmpty(shardingValues)) {
//            for (LocalDateTime date : shardingValues) {
//                //获取日期时间所在的月份
//                int str = date.getMonth().getValue();
//                //添加记录所在分表表名集合
//                shardingSuffix.add(shardingValue.getLogicTableName() + "_" + str);
//            }
//        }
//        return shardingSuffix;

        Set<String> tables = getTables(shardingValue, availableTargetNames.size());
        if (!CollectionUtils.isEmpty(tables)) {
            return tables;
        }
        throw new UnsupportedOperationException();
    }

    private Set<String> getTables(ComplexKeysShardingValue<String> complexKeysShardingValue, Integer size) {
        Set<String> tables = new HashSet<>();
        // 分片条件, 包含多个分片键的值
        Map<String, Collection<String>> map = complexKeysShardingValue.getColumnNameAndShardingValuesMap();
        map.forEach((k, v) -> {
            // 获取Value
            v.forEach(value -> {
                tables.add(complexKeysShardingValue.getLogicTableName() + "_" + value.hashCode() % size);
            });
        });
        return tables;
    }
}
