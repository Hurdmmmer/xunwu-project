package com.youjian.xunwu.comm.utils;

import com.youjian.xunwu.comm.entity.User;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类
 */
public class SecurityKit {
    public static final Long SUPER_ADMIN = 999L;

    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Object principal = authentication.getPrincipal();
            User user = (User) principal;
            if (user == null) {
                throw new AuthenticationCredentialsNotFoundException("当前没有用户登录. 系统异常!!~");
            }
            return user.getId();
        }
        throw new AuthenticationCredentialsNotFoundException("当前没有用户登录. 系统异常!!~");
    }

    public static Authentication getCurrentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            return authentication;
        }
        return null;
    }
}
