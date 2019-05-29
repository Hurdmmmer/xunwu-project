package com.youjian.xunwu.security.mobile;

import com.youjian.xunwu.comm.security.SMSAuthenToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 基于短信登录验证
 */
public class SMSLoginFilter extends AbstractAuthenticationProcessingFilter {

    private final static String SPRING_SECURITY_FORM_SMS_CODE_KEY = "telephone";

    /**
     * 配置拦截的url
     */
    public SMSLoginFilter() {
        super(new AntPathRequestMatcher("/login/mobile", "POST"));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {

        String mobileNo = request.getParameter(SPRING_SECURITY_FORM_SMS_CODE_KEY);

        if (mobileNo == null) {
            mobileNo = "";
        }
        // 模仿 UsernamePasswordAuthenticationFilter 做法
        SMSAuthenToken smsAuthenToken = new SMSAuthenToken(mobileNo);
        this.setDetails(request, smsAuthenToken);
        return this.getAuthenticationManager().authenticate(smsAuthenToken);
    }

    protected void setDetails(HttpServletRequest request, SMSAuthenToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }
}
