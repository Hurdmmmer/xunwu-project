package com.youjian.xunwu.security;

import com.youjian.xunwu.comm.entity.User;
import com.youjian.xunwu.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 自定义 spring security  基于数据库认证实现
 * 实现接口 {@link AuthenticationProvider}
 */
@Component("authProvider")
public class DefaultAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private IUserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = (String) authentication.getCredentials();
        User dbUser = userService.findUserByName(username);
        if (dbUser == null) {
            throw new AuthenticationCredentialsNotFoundException("该用户不存在");
        }

        if (passwordEncoder.matches(password, dbUser.getPassword())) {
            return new UsernamePasswordAuthenticationToken(dbUser, dbUser.getPassword(), dbUser.getAuthorities());
        }

        throw new BadCredentialsException("密码错误");
    }

    @Override
    public boolean supports(Class<?> aClass) {
        // 支持账号密码登录
        return aClass.equals(UsernamePasswordAuthenticationToken.class);
    }
}
