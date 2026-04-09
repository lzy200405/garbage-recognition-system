package edu.hubu.grs;


import edu.hubu.grs.entity.User;
import edu.hubu.grs.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void testRegisterSuccess() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("123456");

        boolean result = userService.register(user);

        assertTrue(result, "注册应该成功");
    }

    @Test
    void testRegisterDuplicate() {
        User user1 = new User();
        user1.setUsername("duplicateUser");
        user1.setPassword("123456");
        userService.register(user1);

        User user2 = new User();
        user2.setUsername("duplicateUser");
        user2.setPassword("123456");

        boolean result = userService.register(user2);

        assertFalse(result, "重复用户名注册应该失败");
    }

    @Test
    void testLoginSuccess() {
        System.out.println("123456");
    }
}