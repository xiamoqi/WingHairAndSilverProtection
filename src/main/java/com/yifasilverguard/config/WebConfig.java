package com.yifasilverguard.config;

import com.yifasilverguard.interceptor.AuthorizationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthorizationInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")                     // 拦截所有请求
                .excludePathPatterns(
                        "/api/v1/codes/**",                 // 你的配置中的忽略路径
                        "/api/v1/users",                    // 注册接口
                        "/api/v1/users/login",              // 登录接口
                        "/api/v1/password-reset",
                        "/error"
                );
    }
}