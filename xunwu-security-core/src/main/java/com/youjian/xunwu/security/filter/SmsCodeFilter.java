package com.youjian.xunwu.security.filter;

import com.youjian.xunwu.security.exception.ValidateCodeException;
import com.youjian.xunwu.security.handler.LoginFailHandler;
import com.youjian.xunwu.security.mobile.ValidateCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.social.connect.web.HttpSessionSessionStrategy;
import org.springframework.social.connect.web.SessionStrategy;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
@Slf4j
public class SmsCodeFilter extends OncePerRequestFilter implements InitializingBean {

    private LoginFailHandler failHandler;

    public SmsCodeFilter(LoginFailHandler loginFailHandler) {
        this.failHandler = loginFailHandler;
    }

    private static final SessionStrategy sessionStrategy = new HttpSessionSessionStrategy();

    private final AntPathMatcher pathPattern = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {

        if (pathPattern.match("/login/mobile", httpServletRequest.getRequestURI())) {
            try {
                validate(new ServletWebRequest(httpServletRequest));
            } catch (ValidateCodeException e) {
                failHandler.onAuthenticationFailure(httpServletRequest, httpServletResponse, e);
                return;
            }
        }

        // 否则执行下面的拦截器,不进行校验
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    /**
     * 校验验证码
     * @param request 请求包装对象
     * @throws ValidateCodeException
     */
    private void validate(ServletWebRequest request) throws ValidateCodeException {
        // session 取出验证码
        ValidateCode smsCode = (ValidateCode) sessionStrategy.getAttribute(request, ValidateCode.SESSION_SMS_CODE_KEY);
        // 请求中获取验证码
        String codeInRequest = null;
        try {
            codeInRequest = ServletRequestUtils.getStringParameter(request.getRequest(), "smsCode");
        } catch (ServletRequestBindingException e) {
            log.error("Failed to get verification code: {}", e.getMessage(), e);
        }

        if (codeInRequest == null || codeInRequest.trim().length()== 0) {
            throw new ValidateCodeException("验证码不能为空");
        }
        if (smsCode == null) {
            throw new ValidateCodeException("验证码不存在");
        }
        if (smsCode.isExpired()) {
            // 过期移出该验证码
            sessionStrategy.removeAttribute(request, ValidateCode.SESSION_SMS_CODE_KEY);
            throw new ValidateCodeException("验证码已过期");
        }
        if (!codeInRequest.equalsIgnoreCase(smsCode.getCode())) {
            throw new ValidateCodeException("验证码不匹配");
        }

        sessionStrategy.removeAttribute(request, ValidateCode.SESSION_SMS_CODE_KEY);
    }
}
