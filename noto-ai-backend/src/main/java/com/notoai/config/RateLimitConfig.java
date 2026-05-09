package com.notoai.config;

import com.google.common.util.concurrent.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitConfig implements HandlerInterceptor {

    private final RateLimiter rateLimiter = RateLimiter.create(10.0);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        if (!rateLimiter.tryAcquire()) {
            response.setStatus(429);
            return false;
        }
        return true;
    }
}
