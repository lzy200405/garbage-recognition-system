package edu.hubu.grs.controller;

import cn.dev33.satoken.stp.StpUtil;
import edu.hubu.grs.common.Result;
import edu.hubu.grs.service.KnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/knowledge")
public class KnowledgeController {

    //查询近期高频种类，调用api，返回生成的科普

    @Autowired
    private KnowledgeService knowledgeService;

    @GetMapping("/generate")
    public CompletableFuture<Result<String>> generate() {
        Long userId = StpUtil.getLoginIdAsLong();

        // 返回 CompletableFuture，Spring MVC 会自动处理异步
        return knowledgeService.generateKnowledge(userId)
                .thenApply(result -> Result.success(result))
                .exceptionally(e -> {
                    System.out.println("生成失败");
                    return Result.fail("生成失败: " + e.getMessage());
                });
    }
}
