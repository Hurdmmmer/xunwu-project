package com.youjian.xunwu.security.config;

import com.youjian.xunwu.security.handler.LoginFailHandler;
import com.youjian.xunwu.security.handler.LoginSuccessHandler;
import com.youjian.xunwu.security.mobile.SMSAuthenticationProvider;
import com.youjian.xunwu.security.mobile.SMSLoginFilter;
import com.youjian.xunwu.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;


/**
 * 配置自定义短信验证配置
 */
@Component
public class SmscodeAuthenticationSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    @Autowired
    private IUserService userDetailsService;

    @Autowired
    private LoginFailHandler failHandler;
    @Autowired
    private LoginSuccessHandler loginSuccessHandler;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        SMSLoginFilter smsLoginFilter = new SMSLoginFilter();
        // 设置成功失败过滤器
        smsLoginFilter.setAuthenticationFailureHandler(failHandler);
        smsLoginFilter.setAuthenticationSuccessHandler(loginSuccessHandler);
        // 设置授权管理器
        smsLoginFilter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));

        SMSAuthenticationProvider smsAuthenticationProvider = new SMSAuthenticationProvider(userDetailsService);

        // 注册 sms 授权提供者, 再 usernamePassWordFilter 之后
        http.authenticationProvider(smsAuthenticationProvider)
                .addFilterAfter(smsLoginFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
