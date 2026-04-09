package edu.hubu.grs.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.exception.BackResultException;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> {
            try {
                HttpServletRequest request = (HttpServletRequest) SaHolder.getRequest().getSource();

                // 放行OPTIONS请求
                if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                    return;
                }

                // 放行登录和注册接口（不需要token）
                SaRouter.match("/auth/login", "/auth/register").stop();

                // 其他请求进行登录校验
                SaRouter.match("/**").check(r -> StpUtil.checkLogin());

            } catch (BackResultException e) {
                // Satoken 拦截返回时，手动添加CORS头（确保401响应也有CORS头）
                HttpServletResponse response = (HttpServletResponse) SaHolder.getResponse().getSource();
                HttpServletRequest request = (HttpServletRequest) SaHolder.getRequest().getSource();

                String origin = request.getHeader("Origin");
                if (origin != null && (origin.contains("localhost") || origin.contains("127.0.0.1"))) {
                    response.setHeader("Access-Control-Allow-Origin", origin);
                    response.setHeader("Access-Control-Allow-Credentials", "true");
                    response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                    response.setHeader("Access-Control-Allow-Headers", "satoken, Authorization, Content-Type");
                }

                // 重新抛出异常让Satoken处理返回
                throw e;
            }
        })).addPathPatterns("/**");
    }
}