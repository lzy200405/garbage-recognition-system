package edu.hubu.grs.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.hubu.grs.common.Result;
import edu.hubu.grs.entity.RecognitionRecord;
import edu.hubu.grs.service.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/record")
public class RecordController {

    //分页查询和删除

    @Autowired
    private RecordService recordService;


    @GetMapping("/page")
    public Result<Page<RecognitionRecord>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

        Long userId = StpUtil.getLoginIdAsLong();

        Page<RecognitionRecord> pageData =
                recordService.getUserRecordsByPage(userId, pageNum, pageSize);

        return Result.success(pageData);
    }
}
