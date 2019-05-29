package com.youjian.xunwu.security.config;

import com.youjian.xunwu.security.DefaultAuthenticationProvider;
import com.youjian.xunwu.security.handler.LoginFailHandler;
import com.youjian.xunwu.security.handler.LoginSuccessHandler;
import com.youjian.xunwu.security.mobile.AdminLoginFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

/**
 *  测试配置管理员登录鉴权
 */

@Component
public class AdminAuthenticationSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {


    @Autowired
    private LoginFailHandler failHandler;
    @Autowired
    private LoginSuccessHandler loginSuccessHandler;
    @Autowired
    private DefaultAuthenticationProvider defaultAuthenticationProvider;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        AdminLoginFilter adminLoginFilter = new AdminLoginFilter();
        // 设置成功失败过滤器
        adminLoginFilter.setAuthenticationFailureHandler(failHandler);
        adminLoginFilter.setAuthenticationSuccessHandler(loginSuccessHandler);
        // 设置授权管理器
        adminLoginFilter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));

//        DefaultAuthenticationProvider defaultAuthenticationProvider = new DefaultAuthenticationProvider();
        // 注册 sms 授权提供者, 再 usernamePasswordFilter 之后
        http.authenticationProvider(defaultAuthenticationProvider)
                .addFilterAfter(adminLoginFilter, UsernamePasswordAuthenticationFilter.class);

    }
}
