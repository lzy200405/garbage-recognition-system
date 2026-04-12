package edu.hubu.grs.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.hubu.grs.entity.User;
import edu.hubu.grs.mapper.UserMapper;
import edu.hubu.grs.service.UserService;
import edu.hubu.grs.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;

import static edu.hubu.grs.utils.PasswordEncoderUtil.BCencode;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisUtil redisUtil;


    @Override
    public boolean register(User user) {
        // 校验用户名是否存在
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", user.getUsername());

        User exist = userMapper.selectOne(wrapper);
        if (exist != null) {
            return false;
        }

        // 保存用户
        return userMapper.insert(user) > 0;
    }

    @Override
    public String login(String username, String password) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);

        User user = userMapper.selectOne(wrapper);

        if (user == null || !BCrypt.checkpw(password,user.getPassword())) {
            return null;
        }

        // sa-token登录
        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();

        // 写入Redis
        redisUtil.set("login:user:" + user.getId(), token, 3600);

        return token;
    }

    @Override
    public User getUserById(Long userId) {
        String key = "user:info:" + userId;

        // 先查Redis
        String cache = redisUtil.get(key);
        if (cache != null) {
            return JSON.parseObject(cache, User.class);
        }

        // 查数据库
        User user = userMapper.selectById(userId);

        // 写入缓存
        if (user != null) {
            redisUtil.set(key, JSON.toJSONString(user), 3600);
        }

        return user;
    }

    @Override
    public void logout(Long userId) {
        StpUtil.logout(userId);

        // 删除Redis缓存
        redisUtil.delete("login:user:" + userId);
        redisUtil.delete("user:info:" + userId);
    }
}
