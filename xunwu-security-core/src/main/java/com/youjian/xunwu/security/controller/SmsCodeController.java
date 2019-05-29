package com.youjian.xunwu.security.controller;

import com.youjian.xunwu.comm.basic.ApiResponse;
import com.youjian.xunwu.security.mobile.ValidateCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.social.connect.web.HttpSessionSessionStrategy;
import org.springframework.social.connect.web.SessionStrategy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Random;

@RestController
@Slf4j
public class SmsCodeController {

    private final SessionStrategy sessionStrategy = new HttpSessionSessionStrategy();

    @GetMapping("/sms/code")
    public ApiResponse smsCode(@RequestParam("telephone")String telephone, HttpServletRequest request) {
        if (telephone == null || telephone.trim().length() == 0) {
            throw new IllegalArgumentException("手机号码错误");
        }
        Random random = new Random(System.currentTimeMillis());
        StringBuilder smsCode = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int value = random.nextInt(10);
            smsCode.append(value);
        }

        log.info("手机验证码是: {}", smsCode.toString());
        ValidateCode validateCode = new ValidateCode(smsCode.toString(), 60);
        sessionStrategy.setAttribute(new ServletWebRequest(request), ValidateCode.SESSION_SMS_CODE_KEY, validateCode);

        return ApiResponse.ofSuccess(null);
    }
}
