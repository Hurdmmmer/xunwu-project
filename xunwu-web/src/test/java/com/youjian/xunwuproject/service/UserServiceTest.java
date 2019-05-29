package com.youjian.xunwuproject.service;

import com.youjian.xunwu.comm.entity.User;
import com.youjian.xunwu.service.IUserService;
import com.youjian.xunwuproject.XunwuProjectApplicationTests;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class UserServiceTest extends XunwuProjectApplicationTests {
    @Autowired
    private IUserService userService;

    @Test
    public void findUser() {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = "$2a$10$cKTYs9l70OAfBoNBOHttl.EOL2VMlxNBKHpCBaP7UJaAu/.S7e8Ku";

        System.out.println("encode = " + encode);

        boolean matches = bCryptPasswordEncoder.matches("123456", encode);
        System.out.println("matches = " + matches);

    }
}
