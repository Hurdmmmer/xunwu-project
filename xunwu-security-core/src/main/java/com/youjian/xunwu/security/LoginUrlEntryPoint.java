package com.youjian.xunwu.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 基于用户角色控制登录页面的控制器
 * USER 进入 user 登录页面
 * ADMIN 进入 admin 登录页面
 */
public class LoginUrlEntryPoint extends LoginUrlAuthenticationEntryPoint {

    private final Map<String, String> entryPointUrlMap = new HashMap<>();

    private final PathMatcher pathMatcher = new AntPathMatcher();

    private final RequestCache requestCache = new HttpSessionRequestCache();

    /**
     * @param loginFormUrl 默认的登录入口
     */
    public LoginUrlEntryPoint(String loginFormUrl) {
        super(loginFormUrl);
        // 普通用户登录页面映射
        entryPointUrlMap.put("/user/**", "/user/login");
        // 管理员登录页面樱色
        entryPointUrlMap.put("/admin/**", "/admin/login");
    }

    /** 重写此方式, 根据对应 URL 转发到对应的登录页面 */
    @Override
    public String determineUrlToUseForThisRequest(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) {

        String requestURI = request.getRequestURI();
        // 根据访问路径匹配登录路径
        for (Map.Entry<String, String> entry : entryPointUrlMap.entrySet()) {
            if (pathMatcher.match(entry.getKey(), requestURI)) {
                return entry.getValue();
            }
        }
        return super.determineUrlToUseForThisRequest(request, response, exception);
    }
}
