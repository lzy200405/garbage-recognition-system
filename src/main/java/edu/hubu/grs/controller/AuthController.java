package edu.hubu.grs.controller;

import cn.dev33.satoken.stp.StpUtil;
import edu.hubu.grs.common.Result;
import edu.hubu.grs.entity.User;
import edu.hubu.grs.service.UserService;
import edu.hubu.grs.utils.PasswordEncoderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

import static edu.hubu.grs.utils.PasswordEncoderUtil.BCencode;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoderUtil passwordEncoderUtil;

    @PostMapping("/register")
    public Result<String> register(@RequestBody User user) {
        if(user.getUsername() == null || user.getPassword() == null){
            return Result.fail("用户名或密码不能为空");
        }
        User Encodeuser = new User();
        Encodeuser.setUsername(user.getUsername());
        Encodeuser.setPassword( BCencode(user.getPassword()));
        Encodeuser.setEmail(user.getEmail());
        boolean success = userService.register(Encodeuser);

        if(success){
            return Result.success("注册成功");
        }else {
            return Result.fail("用户名已存在");
        }
    }

    @PostMapping("/login")
    public Result<Object> login(@RequestBody Map<String, String> loginRequest) {

        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        String token = userService.login(username, password);

        if(token == null){
            System.out.println("500");
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
