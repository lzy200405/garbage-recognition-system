package edu.hubu.grs.controller;

import cn.dev33.satoken.stp.StpUtil;

import edu.hubu.grs.common.Result;
import edu.hubu.grs.entity.RecognitionRecord;
import edu.hubu.grs.service.RecognitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/recognition")
public class RecognitionController {

    //上传 → 存储 → 识别 → 入库 → 返回结果

    @Autowired
    private RecognitionService recognitionService;

    @PostMapping("/upload")
    public Result<RecognitionRecord> upload(@RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return Result.fail("请上传图片");
        }

        if (!file.getContentType().startsWith("image/")) {
            return Result.fail("只能上传图片");
        }

        Long userId = StpUtil.getLoginIdAsLong();

        try {
            RecognitionRecord record = recognitionService.recognize(file, userId);
            return Result.success(record);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
}
