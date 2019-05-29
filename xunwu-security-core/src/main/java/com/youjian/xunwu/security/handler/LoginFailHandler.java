package com.youjian.xunwu.security.handler;

import com.youjian.xunwu.security.LoginUrlEntryPoint;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 登录失败处理器
 */

public class LoginFailHandler extends SimpleUrlAuthenticationFailureHandler {
    /**
     * {@link LoginUrlEntryPoint}
     */
    private final LoginUrlEntryPoint loginUrlEntryPoint;

    public LoginFailHandler(LoginUrlEntryPoint loginUrlEntryPoint) {
        this.loginUrlEntryPoint = loginUrlEntryPoint;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        // 根据登录入口处理程序获取登录目标 url
        String targetUrl = this.loginUrlEntryPoint.determineUrlToUseForThisRequest(request, response, exception);
        String smsCode = request.getParameter("smsCode");
        // 这里后端校验账号密码, 传递参数到 前端, 前端判断 authError 显示错误信息
        if (smsCode != null && smsCode.trim().length() > 0) {
            targetUrl += "?smsCodeError";
        } else {
            targetUrl += "?authError";
        }
        // 把 targetUrl 设置为默认错误跳转 url
        super.setDefaultFailureUrl(targetUrl);
        super.onAuthenticationFailure(request, response, exception);
    }
}
