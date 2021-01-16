package com.luxf.sharding.utils;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Objects;

/**
 * 解析SpEL表达式
 *
 * @author 小66
 **/
public class ExpressionParseUtils {

    /**
     * converter、 A shared default {@code ConversionService} instance, lazily building it once needed.
     */
    private static ConversionService converter = DefaultConversionService.getSharedInstance();

    /**
     * SpEL parser. Instances are reusable and thread-safe.
     *
     * @see SpelExpressionParser
     */
    private static ExpressionParser parser = new SpelExpressionParser();

    private static ParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    /**
     * 获取SpEL表达式的值
     */
    public static <T> T getParseValue(String spelExpression, Method method, Object[] args, Class<T> target) {
        return converter.convert(parseExpression(spelExpression, method, args), target);
    }

    public static String getParseValue(String spelExpression, Method method, Object[] args) {
        return converter.convert(parseExpression(spelExpression, method, args), String.class);
    }

    private static Object parseExpression(String spelExpression, Method method, Object[] args) {
        // 不可直接返回、 SpEL可以解析字符串. 'answer'
        // if (Objects.isNull(args) || args.length == 0) {
        //     return null;
        // }

        String[] paramNameArr = discoverer.getParameterNames(method);

        /**
         * 创建EvaluationContext、如有需要,可自定义一个{@link org.springframework.context.expression.MethodBasedEvaluationContext}的实现类.
         *
         * 不解析result, 则需要排除掉result. --> evaluationContext.addUnavailableVariable("result");
         * @see org.springframework.cache.interceptor.CacheEvaluationContext#lookupVariable(String)
         * @see org.springframework.cache.interceptor.CacheOperationExpressionEvaluator#createEvaluationContext(Collection, Method, Object[], Object, Class, Method, Object, BeanFactory)
         */
        Expression expression = parser.parseExpression(spelExpression);
        if (Objects.nonNull(paramNameArr)) {
            EvaluationContext context = new StandardEvaluationContext();
            for (int i = 0; i < paramNameArr.length; i++) {
                context.setVariable(paramNameArr[i], args[i]);
            }
            return expression.getValue(context);
        }
        return expression.getValue();
    }
}
