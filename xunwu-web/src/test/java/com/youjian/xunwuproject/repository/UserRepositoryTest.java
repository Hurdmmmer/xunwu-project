package com.youjian.xunwuproject.repository;

import com.youjian.xunwu.comm.entity.User;
import com.youjian.xunwu.dao.UserRepository;
import com.youjian.xunwuproject.XunwuProjectApplicationTests;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

public class UserRepositoryTest extends XunwuProjectApplicationTests {
    @Autowired
    UserRepository userRepository;

    @Test
    public void findOne() {
        Optional<User> user = userRepository.findById(1L);
        User dbUser = user.get();
        Assert.assertEquals(dbUser.getName(), "wali");
    }
}
