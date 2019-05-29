package com.youjian.xunwuproject;

import com.youjian.xunwu.web.XunwuProjectApplication;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = XunwuProjectApplication.class)
// 指定使用 test 环境进行测试
@ActiveProfiles("dev")
public class XunwuProjectApplicationTests {


}
