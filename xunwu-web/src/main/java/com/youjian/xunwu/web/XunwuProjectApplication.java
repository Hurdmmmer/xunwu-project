package com.youjian.xunwu.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = "com.youjian.xunwu")
@EnableTransactionManagement
public class XunwuProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(XunwuProjectApplication.class, args);
    }

}
