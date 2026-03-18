package edu.hubu.grs.controller;

import cn.dev33.satoken.stp.StpUtil;
import edu.hubu.grs.common.Result;
import edu.hubu.grs.service.KnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/knowledge")
public class KnowledgeController {

    //查询近期高频种类，调用api，返回生成的科普

    @Autowired
    private KnowledgeService knowledgeService;

    @GetMapping("/generate")
    public Result<String> generate() {

        Long userId = StpUtil.getLoginIdAsLong();

        String res = knowledgeService.generateKnowledge(userId);

        return Result.success(res);
    }
}
