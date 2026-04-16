package com.aining.aspect;

import com.aining.annotation.RateLimit;
import com.aining.exception.BusinessException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 * Redis + Lua 滑动窗口限流切面，支持 SpEL 表达式解析 key。
 */
@Aspect
@Component
public class RateLimitAspect {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    /**
     * Lua 脚本：滑动窗口限流
     * 1. 移除窗口外的旧记录
     * 2. 获取当前窗口内记录数
     * 3. 若未超过阈值，则添加当前时间戳并设置 key 过期时间
     * 返回 1 表示允许访问，0 表示触发限流
     */
    private static final String LUA_SCRIPT =
            "local key = KEYS[1] " +
            "local window = tonumber(ARGV[1]) " +
            "local threshold = tonumber(ARGV[2]) " +
            "local now = tonumber(ARGV[3]) " +
            "redis.call('ZREMRANGEBYSCORE', key, 0, now - window * 1000) " +
            "local count = redis.call('ZCARD', key) " +
            "if count < threshold then " +
            "    redis.call('ZADD', key, now, now) " +
            "    redis.call('EXPIRE', key, window) " +
            "    return 1 " +
            "else " +
            "    return 0 " +
            "end";

    private final DefaultRedisScript<Long> redisScript;

    public RateLimitAspect() {
        this.redisScript = new DefaultRedisScript<>();
        this.redisScript.setScriptText(LUA_SCRIPT);
        this.redisScript.setResultType(Long.class);
    }

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint point, RateLimit rateLimit) throws Throwable {
        String key = buildKey(point, rateLimit);
        long now = System.currentTimeMillis();

        List<String> keys = Collections.singletonList(key);
        Long result = stringRedisTemplate.execute(
                redisScript,
                keys,
                String.valueOf(rateLimit.timeWindow()),
                String.valueOf(rateLimit.maxRequests()),
                String.valueOf(now)
        );

        if (result == null || result == 0L) {
            throw new BusinessException(rateLimit.message());
        }

        return point.proceed();
    }

    private String buildKey(ProceedingJoinPoint point, RateLimit rateLimit) {
        StringBuilder sb = new StringBuilder(rateLimit.prefix());

        if (StringUtils.hasText(rateLimit.key())) {
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            String[] paramNames = discoverer.getParameterNames(method);
            Object[] args = point.getArgs();

            EvaluationContext context = new StandardEvaluationContext();
            if (paramNames != null) {
                for (int i = 0; i < paramNames.length; i++) {
                    context.setVariable(paramNames[i], args[i]);
                }
            }
            // 兼容 HttpServletRequest 变量名称
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof HttpServletRequest) {
                    context.setVariable("request", args[i]);
                }
            }

            Object value = parser.parseExpression(rateLimit.key()).getValue(context);
            if (value == null || !StringUtils.hasText(value.toString())) {
                throw new BusinessException("限流 key 解析失败");
            }
            sb.append(value.toString());
        } else {
            sb.append(methodFullName(point));
        }

        return sb.toString();
    }

    private String methodFullName(ProceedingJoinPoint point) {
        return point.getTarget().getClass().getName() + ":" + point.getSignature().getName();
    }
}
