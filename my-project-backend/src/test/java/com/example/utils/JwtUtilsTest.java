package com.example.utils;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class JwtUtilsTest {

    @Test
    void deleteTokenUsesMilliseconds() throws Exception {
        JwtUtils utils = new JwtUtils();
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(template.opsForValue()).thenReturn(valueOps);
        when(template.hasKey(anyString())).thenReturn(false);

        Field templateField = JwtUtils.class.getDeclaredField("template");
        templateField.setAccessible(true);
        templateField.set(utils, template);

        Method deleteToken = JwtUtils.class.getDeclaredMethod("deleteToken", String.class, Date.class);
        deleteToken.setAccessible(true);

        Date future = new Date(System.currentTimeMillis() + 1000);
        deleteToken.invoke(utils, "abc", future);

        verify(valueOps).set(eq(Const.JWT_BLACK_LIST + "abc"), eq(""), anyLong(), eq(TimeUnit.MILLISECONDS));
    }
}
