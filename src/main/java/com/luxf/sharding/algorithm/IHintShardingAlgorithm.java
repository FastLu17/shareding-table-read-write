package com.luxf.sharding.algorithm;

import com.alibaba.druid.util.StringUtils;
import org.apache.shardingsphere.api.sharding.hint.HintShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.hint.HintShardingValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author 小66
 * @Description
 * @create 2021-01-11 21:50
 **/
public class IHintShardingAlgorithm implements HintShardingAlgorithm<Long> {
    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, HintShardingValue<Long> shardingValue) {
        // Hint分片算法的ShardingValue有两种具体类型: ListShardingValue和RangeShardingValue
        // 使用哪种取决于HintManager.addDatabaseShardingValue(String, String, ShardingOperator,...)中ShardingOperator的类型
        System.out.println("shardingValue=" + shardingValue);
        System.out.println("availableTargetNames=" + availableTargetNames);

        List<String> shardingResult = new ArrayList<>();

        for (String targetName : availableTargetNames) {
            String suffix = targetName.substring(targetName.length() - 1);
            if (StringUtils.isNumber(suffix)) {
                for (Long value : shardingValue.getValues()) {
                    if (value % 4 == Long.parseLong(suffix)) {
                        shardingResult.add(targetName);
                        break;
                    }
                }
            }
            if (!shardingResult.isEmpty()) {
                break;
            }
        }

        return shardingResult;
    }
}
