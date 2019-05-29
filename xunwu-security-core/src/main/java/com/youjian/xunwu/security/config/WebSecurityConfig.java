package com.youjian.xunwu.security.config;

import com.youjian.xunwu.security.DefaultAuthenticationProvider;
import com.youjian.xunwu.security.LoginUrlEntryPoint;
import com.youjian.xunwu.security.filter.SmsCodeFilter;
import com.youjian.xunwu.security.handler.LoginFailHandler;
import com.youjian.xunwu.security.handler.LoginSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private DefaultAuthenticationProvider defaultAuthenticationProvider;

    @Autowired
    private SmscodeAuthenticationSecurityConfig smscodeAuthenticationSecurityConfig;

    @Autowired
    private AdminAuthenticationSecurityConfig adminAuthenticationSecurityConfig;

    /**
     * HTTP security 安全配置, 注意: 跳转的路径需要 controller 支持
     *
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 添加短信验证码过滤器, 再账号密码之前认证
        http.addFilterBefore(smsCodeFilter(), UsernamePasswordAuthenticationFilter.class);

        http.authorizeRequests()
                .antMatchers("/static/**").permitAll()
                .antMatchers("/admin/login").permitAll()
                .antMatchers("/user/login").permitAll()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/api/user/**").hasAnyRole("ADMIN", "USER")
                .antMatchers("/user/**").hasAnyRole("ADMIN", "USER")
                .and()
                .formLogin()
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/")
                .successHandler(loginSuccessHandler())   // 设置登录成功处理器, 根据不同的权限跳转不同的页面
                .failureHandler(loginFailHandler()) // 配置登录错误处理器
                .and()
                .logout() // 登出处理
                .logoutUrl("/logout")
                .logoutSuccessUrl("/logout/page")
                .deleteCookies("JSESSIONID")  //删除 cookie session 回话失效
                .invalidateHttpSession(true)
                .and()   // 设置根据访问路径跳转登录界面
                .exceptionHandling()
                .authenticationEntryPoint(entryPoint()) //配置根据角色跳转不同页面
                .accessDeniedPage("/403")  //配置无权访问的页面

                .and()
                .apply(smscodeAuthenticationSecurityConfig)  // 配置短信登录
                .and()
                .apply(adminAuthenticationSecurityConfig);

//
        http.csrf().disable();
        http.headers().frameOptions().sameOrigin(); // 开启同源策略 iframe 就可以使用了
//        http.authorizeRequests().antMatchers("/**").permitAll();
    }



    @Override
    // 自定义认证逻辑
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .authenticationProvider(defaultAuthenticationProvider)
                .eraseCredentials(true)  // 檫除认证过程的密码
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Bean
    public LoginFailHandler loginFailHandler() {
        // 注入登录入口处理程序
        return new LoginFailHandler(entryPoint());
    }

    @Bean
    public SmsCodeFilter smsCodeFilter() {
       return new SmsCodeFilter(loginFailHandler());
    }

    @Bean
    public LoginSuccessHandler loginSuccessHandler() {
        return new LoginSuccessHandler();
    }

    @Bean
    public LoginUrlEntryPoint entryPoint() {
        return new LoginUrlEntryPoint("/user/login");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
