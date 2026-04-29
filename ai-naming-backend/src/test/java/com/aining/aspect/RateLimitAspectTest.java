package com.aining.aspect;

import com.aining.annotation.RateLimit;
import com.aining.exception.BusinessException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RateLimitAspectTest {

    private StringRedisTemplate redisTemplate;
    private RateLimitAspect aspect;
    private ProceedingJoinPoint point;
    private MethodSignature signature;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        redisTemplate = mock(StringRedisTemplate.class);
        aspect = new RateLimitAspect();
        // 通过反射注入 mock 的 redisTemplate
        org.springframework.util.ReflectionUtils.findField(RateLimitAspect.class, "stringRedisTemplate")
                .setAccessible(true);
        try {
            java.lang.reflect.Field field = RateLimitAspect.class.getDeclaredField("stringRedisTemplate");
            field.setAccessible(true);
            field.set(aspect, redisTemplate);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        point = mock(ProceedingJoinPoint.class);
        signature = mock(MethodSignature.class);
        when(point.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(DummyController.class.getMethod("testMethod", HttpServletRequest.class));
        when(signature.getParameterNames()).thenReturn(new String[]{"request"});
        when(point.getArgs()).thenReturn(new Object[]{mock(HttpServletRequest.class)});
    }

    @Test
    void shouldAllowWhenUnderLimit() throws Throwable {
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), anyString(), anyString(), anyString()))
                .thenReturn(1L);
        when(point.proceed()).thenReturn("success");

        RateLimit rateLimit = DummyController.class.getMethod("testMethod", HttpServletRequest.class)
                .getAnnotation(RateLimit.class);

        Object result = aspect.around(point, rateLimit);
        assertEquals("success", result);
        verify(point).proceed();
    }

    @Test
    void shouldBlockWhenOverLimit() throws NoSuchMethodException {
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), anyString(), anyString(), anyString()))
                .thenReturn(0L);

        RateLimit rateLimit = DummyController.class.getMethod("testMethod", HttpServletRequest.class)
                .getAnnotation(RateLimit.class);

        BusinessException ex = assertThrows(BusinessException.class, () -> aspect.around(point, rateLimit));
        assertEquals("请求过于频繁，请稍后重试", ex.getMessage());
    }

    static class DummyController {
        @RateLimit(key = "#request.getHeader('X-Openid')", timeWindow = 60, maxRequests = 3)
        public String testMethod(HttpServletRequest request) {
            return "ok";
        }
    }
}
