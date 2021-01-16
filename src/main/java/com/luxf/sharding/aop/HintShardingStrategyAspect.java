package com.luxf.sharding.aop;

import com.luxf.sharding.annotations.HintDatabaseOnly;
import com.luxf.sharding.annotations.HintDatabaseStrategy;
import com.luxf.sharding.annotations.HintShardingStrategy;
import com.luxf.sharding.annotations.HintTableStrategy;
import com.luxf.sharding.bean.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.hint.HintManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.CachedExpressionEvaluator;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * TODO: 可以单独兼容{@link HintShardingStrategy}内部的每一个注解{@link HintDatabaseOnly,HintDatabaseStrategy,HintTableStrategy}、
 * <p>
 * 注意:同一线程内, HintManager只能被创建一次.
 *
 * @author 小66
 * @Description
 * @create 2021-01-09 17:25
 * @see HintManager#getInstance()
 **/
@Aspect
@Component
@Slf4j
public class HintShardingStrategyAspect {

    /**
     * converter、 A shared default {@code ConversionService} instance, lazily building it once needed.
     */
    private ConversionService converter = DefaultConversionService.getSharedInstance();

    /**
     * SpEL parser. Instances are reusable and thread-safe.
     *
     * @see SpelExpressionParser
     */
    private ExpressionParser parser = new SpelExpressionParser();

    // 获取被拦截方法参数名列表、(SpringCache使用的是 DefaultParameterNameDiscoverer)
    // ParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();
    private ParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    @Pointcut(value = "@annotation(com.luxf.sharding.annotations.HintShardingStrategy)")
    public void pointCut() {
    }

    /**
     * 可以直接获取注解参数、不用通过反射获取.
     * '@annotation(hintStrategy)'中的值, 需要和doAround()方法中的注解参数名称相同(必须相同,但是名称任意).
     */
    @Around(value = "pointCut() && @annotation(hintStrategy)")
    public Object doAround(ProceedingJoinPoint pjp, HintShardingStrategy hintStrategy) throws Throwable {
        try (HintManager instance = HintManager.getInstance()) {
            boolean masterRouteOnly = hintStrategy.masterRouteOnly();
            if (masterRouteOnly) {
                instance.setMasterRouteOnly();
            }
            HintDatabaseOnly databaseOnly = hintStrategy.databaseShardingOnly();
            if (databaseOnly.databaseShardingOnly() && databaseOnly.value() >= 0) {
                instance.setDatabaseShardingValue(databaseOnly.value());
            } else {
                MethodSignature signature = (MethodSignature) pjp.getSignature();
                Method method = signature.getMethod();
                Object[] args = pjp.getArgs();

                HintTableStrategy[] tableValues = hintStrategy.tableShardingValues();
                for (HintTableStrategy tableValue : tableValues) {
                    Long value = getLongValue(method, args, tableValue.spelValue());
                    Assert.isTrue(tableValue.divisor() > 0, "sharding divisor must be positive.");
                    instance.addTableShardingValue(tableValue.logicTable(), value % tableValue.divisor());
                }
                HintDatabaseStrategy[] databaseValues = hintStrategy.databaseShardingValues();
                for (HintDatabaseStrategy databaseValue : databaseValues) {
                    Long value = getLongValue(method, args, databaseValue.spelValue());
                    Assert.isTrue(databaseValue.divisor() > 0, "sharding divisor must be positive.");
                    instance.addDatabaseShardingValue(databaseValue.logicTable(), value % databaseValue.divisor());
                }
            }
            return pjp.proceed();
        }
    }

    /**
     * 将SpEL解析的值转换为Long、不正确使用{@link HintTableStrategy,HintDatabaseStrategy}直接抛出异常.
     */
    private Long getLongValue(Method method, Object[] args, String spelExpression) {
        Object parseValue = parseExpression(spelExpression, method, args);
        // 直接convert()、 不同提前调用canConvert()判断.  不能convert, 会抛出异常.
        Long convert = converter.convert(parseValue, Long.TYPE);
        Assert.isTrue(Objects.requireNonNull(convert) >= 0, "sharding value can not be negative.");
        return convert;
    }

    /**
     * 获取SpEL表达式的解析值、
     * <p>
     * TODO: 看看Spring Cache的源码, 如何处理的SpEL表达式、
     *
     * @param spelExpression SPEL表达式
     * @param method         被拦截的方法
     * @param args           被拦截的方法参数
     * @return parse value.
     * see org.springframework.cache.interceptor.CacheOperationExpressionEvaluator#key(String, AnnotatedElementKey, EvaluationContext)
     * see org.springframework.cache.interceptor.CacheOperationExpressionEvaluator#condition(String, AnnotatedElementKey, EvaluationContext)
     * see org.springframework.cache.interceptor.CacheOperationExpressionEvaluator#unless(String, AnnotatedElementKey, EvaluationContext)
     * @see CachedExpressionEvaluator
     */
    private Object parseExpression(String spelExpression, Method method, Object[] args) {
        if (Objects.isNull(args) || args.length == 0) {
            return null;
        }

        String[] paramNameArr = Objects.requireNonNull(discoverer.getParameterNames(method));

        /**
         * 创建EvaluationContext、 如有需要,可自定义一个{@link org.springframework.context.expression.MethodBasedEvaluationContext}的实现类.
         *
         * 需要排除掉result. --> evaluationContext.addUnavailableVariable("result");
         * @see org.springframework.cache.interceptor.CacheEvaluationContext#lookupVariable(String)
         * @see org.springframework.cache.interceptor.CacheOperationExpressionEvaluator#createEvaluationContext(Collection, Method, Object[], Object, Class, Method, Object, BeanFactory)
         */
        // SpEL解析
        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < paramNameArr.length; i++) {
            context.setVariable(paramNameArr[i], args[i]);
        }
        return parser.parseExpression(spelExpression).getValue(context);
    }

    /**
     * SpEl 支持的计算变量：
     * 1）#ai、#pi、#命名参数【i 表示参数下标，从 0 开始】
     * 2）#result：CachePut 操作和后处理 CacheEvict 操作都可使用
     * 3）#root：CacheExpressionRootObject 对象
     *
     * @param args
     */
    public static void main(String[] args) {
        // 创建一个ExpressionParser对象，用于解析表达式
        ExpressionParser parser = new SpelExpressionParser();

        // 最简单的字符串表达式
        Expression exp = parser.parseExpression("'HelloWorld'");
        System.out.println("'HelloWorld'的结果: " + exp.getValue());

        // 调用方法的表达式
        exp = parser.parseExpression("'HelloWorld'.concat('!')");
        System.out.println("'HelloWorld'.concat('!')的结果: " + exp.getValue());

        // 调用对象的getter方法
        exp = parser.parseExpression("'HelloWorld'.bytes");
        System.out.println("'HelloWorld'.bytes的结果: " + exp.getValue());

        // 访问对象的属性(d相当于HelloWorld.getBytes().length)
        exp = parser.parseExpression("'HelloWorld'.bytes.length");
        System.out.println("'HelloWorld'.bytes.length的结果:" + exp.getValue());

        // 使用构造器来创建对象
        exp = parser.parseExpression("new String('helloworld')" + ".toUpperCase()");
        System.out.println("new String('helloworld')" + ".toUpperCase()的结果是: " + exp.getValue(String.class));

        // 以指定对象作为root来计算表达式的值
        // 相当于调用user.name表达式的值
        User user = new User(1L, "CQ", "孙悟空");
        exp = parser.parseExpression("name");
        System.out.println("以user为root, name表达式的值是: " + exp.getValue(user, String.class));

        exp = parser.parseExpression("name=='孙悟空'");
        System.out.println("name=='孙悟空': " + exp.getValue(user, Boolean.TYPE));

        // 创建一个List
        List<String> list = new ArrayList<>();
        list.add("Java");
        list.add("Spring");
        // 创建一个EvaluationContext对象，作为SpEL解析变量的上下文
        EvaluationContext ctx = new StandardEvaluationContext();
        // 设置一个变量
        ctx.setVariable("myList", list);
        // 对集合的第一个元素进行赋值
        Object value = parser.parseExpression("#myList[0]='我爱你中国'").getValue(ctx);
        System.out.println("value = " + value);
        // 下面测试输出
        System.out.println("List更改后的第一个元素的值为:" + list.get(0));

        // 使用三目运算符
        System.out.println(parser.parseExpression("#myList.size()>3 ? 'myList长度大于3':'myList长度不大于3'").getValue(ctx));
    }
}
