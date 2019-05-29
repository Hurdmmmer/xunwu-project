package com.youjian.xunwu.web.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.extras.springsecurity5.dialect.SpringSecurityDialect;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

/**
 * 实现 {@link ApplicationContextAware}, 为了方便的获取 spring web 的上下文 <br/>
 * {@link ApplicationContext}
 */
@Configuration
public class WebMvcConfig implements ApplicationContextAware, WebMvcConfigurer{

    private ApplicationContext applicationContext;

    /** 静态资源配置 */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    /** spring thymeleaf 资源解析器, 配置读取配置文件才能实例化, 否则实例化会出现资源找不到异常*/
    @Bean
    @ConfigurationProperties(prefix = "spring.thymeleaf")
    public SpringResourceTemplateResolver templateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setApplicationContext(this.applicationContext);
        resolver.setCharacterEncoding("UTF-8");
        return resolver;
    }
    /** spring thymeleaf 方言解析器 */
    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        // 支持 spring el 表达式
        templateEngine.setEnableSpringELCompiler(true);
        // 设置资源解析器
        templateEngine.setTemplateResolver(templateResolver());

//        支持 spring security thymeleaf 模板的方言
        SpringSecurityDialect dialect = new SpringSecurityDialect();
        templateEngine.addDialect(dialect);

        return templateEngine;
    }
    /**
     * thymeleaf 视图解析器
     */
    @Bean
    public ThymeleafViewResolver viewResolver(@Autowired ThymeleafProperties thymeleafProperties) {
        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
        viewResolver.setTemplateEngine(templateEngine());
        viewResolver.setCache(thymeleafProperties.isCache());
        return viewResolver;
    }
}
