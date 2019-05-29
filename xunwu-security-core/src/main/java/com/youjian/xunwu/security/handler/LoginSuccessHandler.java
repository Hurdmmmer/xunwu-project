package com.youjian.xunwu.security.handler;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.util.AntPathMatcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * security 登录成功处理器, 继承 {@link SavedRequestAwareAuthenticationSuccessHandler}<br/>
 * 实现 {@link SavedRequestAwareAuthenticationSuccessHandler#onAuthenticationSuccess(HttpServletRequest, HttpServletResponse, Authentication)}
 * <br> 父类方法就是按原先访问的 url 进行跳转, 该方法根据业务场景进行指定跳转
 */

public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private RequestCache requestCache = new HttpSessionRequestCache();


    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
        String redirectUrl = null;
        try {
            redirectUrl = requestCache.getRequest(httpServletRequest, httpServletResponse).getRedirectUrl();
        } catch (Exception e) {
            redirectUrl = httpServletRequest.getRequestURI();
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        // 根据角色跳转指定页面
        String roles = authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(";"));
        if (!roles.isEmpty() && roles.contains("ADMIN") && redirectUrl != null && redirectUrl.contains("/login")) {
            httpServletResponse.sendRedirect("/admin/center");
            return;
        }

        super.onAuthenticationSuccess(httpServletRequest, httpServletResponse, authentication);
    }
}
