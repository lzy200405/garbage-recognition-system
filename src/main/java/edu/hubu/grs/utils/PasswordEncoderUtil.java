package edu.hubu.grs.utils;


import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import java.security.SecureRandom;

/**
 * 密码加密工具类
 * 使用BCrypt加密算法，内置盐值，安全性高
 *
 * @author yourname
 * @date 2024
 */
@Component
public class PasswordEncoderUtil {

    // BCrypt的工作因子，默认10，范围4-31，值越大越安全但性能越低
    private static final int LOG_ROUNDS = 10;

    // 用于生成随机盐值的随机数生成器
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 加密密码
     * @param plainPassword 明文密码
     * @return 加密后的密码（包含盐值）
     * @throws IllegalArgumentException 如果密码为空或长度不足
     */
    public static String BCencode(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }

        if (plainPassword.length() < 6) {
            throw new IllegalArgumentException("密码长度不能少于6位");
        }

        // BCrypt会自动生成随机盐值并包含在结果中
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt(LOG_ROUNDS));
        return hashedPassword;
    }

}