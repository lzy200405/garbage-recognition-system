package edu.hubu.grs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.hubu.grs.entity.User;

public interface UserService extends IService<User> {

    /**
     * 登录
     * @param username    用户名，前端校验不为空
     * @param password    密码，同样前端校验
     * @return  返回Token
     */
    String login(String username, String password);

    /**
     * 注册
     * @param user 用户信息
     * @return 布尔类型
     */
    boolean register(User user);

    /**
     * 获取用户信息
     * @param  userId
     * @return  用户对象
     */
    User getUserById(Long userId);

    /**
     * 登出，清除缓存
     * @param userId
     */
    void logout(Long userId);
}
