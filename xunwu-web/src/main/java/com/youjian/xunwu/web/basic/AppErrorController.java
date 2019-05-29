package com.youjian.xunwu.web.basic;

import com.youjian.xunwu.comm.basic.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.MediaType;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.StandardServletAsyncWebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Web 全局异常处理, 400 500 403
 * spring mvc 实现 {@link ErrorController} 接口
 */
@Controller
public class AppErrorController implements ErrorController {

    private static final String ERROR_PATH = "/error";
    /** 固定错误状态码错误 key */
    private static final String ERROR_STATUS_CODE_STRING = "javax.servlet.error.status_code";

    private ErrorAttributes errorAttributes;

    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }

    @Autowired
    public AppErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    /**
     * Web 页面异常处理
     */
    @RequestMapping(value = ERROR_PATH, produces = MediaType.TEXT_HTML_VALUE)
    public String errorPageHandler(HttpServletRequest request, HttpServletResponse response, Model model) {
        int status = response.getStatus();
        switch (status) {
            case 403:
                return "403";
            case 404:
                return "404";
            case 500:
                return "500";
        }
        return "index";
    }

    /**
     * 除 web 页面以外的异常处理, 如 json xml等
     */
    @RequestMapping(value = ERROR_PATH)
    @ResponseBody
    public ApiResponse errorApiHandler(HttpServletRequest request, HttpServletResponse response) {
        WebRequest requestAttribute = new StandardServletAsyncWebRequest(request, response);
        // 获取报错信息
        Map<String, Object> errorAttributes = this.errorAttributes.getErrorAttributes(requestAttribute, false);
        Integer status = getStatus(request);
        Set<Map.Entry<String, Object>> entries = errorAttributes.entrySet();
        String errorMessage = entries.stream().
                filter(e -> e.getKey().equalsIgnoreCase("error") || e.getKey().equalsIgnoreCase("message"))
                .map(e -> e.getValue() + "").collect(Collectors.joining("; "));

        return ApiResponse.ofMessage(status, errorMessage);
    }

    /** 获取错误状态码 */
    private Integer getStatus(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute(ERROR_STATUS_CODE_STRING);
        if (statusCode != null) {
            return statusCode;
        }
        return 500;
    }


}
