package com.youjian.xunwu.security.mobile;

import com.youjian.xunwu.comm.entity.User;
import com.youjian.xunwu.comm.security.SMSAuthenToken;
import com.youjian.xunwu.service.IUserService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * 短信验证提供者
 */
@AllArgsConstructor
public class SMSAuthenticationProvider implements AuthenticationProvider {

    private IUserService userService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // 使用手机号码查询用户信息
        User dbUser = userService.findUserByName(authentication.getName());
        if (dbUser == null) {
            throw new AuthenticationCredentialsNotFoundException("该用户不存在");
        }
        // 返回自定义短信认证信息
        return new SMSAuthenToken(dbUser, dbUser.getPassword(), dbUser.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> aClass) {
        // 支持 sms 验证
        return SMSAuthenToken.class.isAssignableFrom(aClass);
    }
}
