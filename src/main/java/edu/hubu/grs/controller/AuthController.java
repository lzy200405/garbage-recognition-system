package edu.hubu.grs.controller;

import cn.dev33.satoken.stp.StpUtil;
import edu.hubu.grs.common.Result;
import edu.hubu.grs.entity.User;
import edu.hubu.grs.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<String> register(@RequestBody User user) {
        if(user.getUsername() == null || user.getPassword() == null){
            return Result.fail("用户名或密码不能为空");
        }
        boolean success = userService.register(user);

        if(success){
            return Result.success("注册成功");
        }else {
            return Result.fail("用户名已存在");
        }
    }

    @PostMapping("/login")
    public Result<Object> login(@RequestParam String username,
                                @RequestParam String password) {
        String token = userService.login(username, password);

        if(token == null){
            return Result.fail("用户名或密码错误");
        }

        return Result.success(token);
    }

    @GetMapping("/logout")
    public Result<String> logout() {
        Long userID = StpUtil.getLoginIdAsLong();
        userService.logout(userID);
        return Result.success("退出成功");
    }
}
