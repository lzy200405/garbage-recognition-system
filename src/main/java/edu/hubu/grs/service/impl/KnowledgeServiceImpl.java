package edu.hubu.grs.service.impl;

import edu.hubu.grs.common.Config;
import edu.hubu.grs.entity.RecognitionRecord;
import edu.hubu.grs.service.KnowledgeService;
import edu.hubu.grs.service.RecordService;
import edu.hubu.grs.utils.DeepSeekUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class KnowledgeServiceImpl implements KnowledgeService {

    @Autowired
    private RecordService recordService;

    @Autowired
    private DeepSeekUtil deepSeekUtil;

    @Override
    public CompletableFuture<String> generateKnowledge(Long userId) {
        List<RecognitionRecord> userRecord = recordService.getRecentRecords(userId, Config.SEARCH_LIMIT);
        String garbageType = recordService.getTopGarbageType(userRecord);

        // 直接返回异步结果
        return DeepSeekUtil.chatAsync(garbageType, Config.DEEPSEEK_SYSTEMPROMPT);
    }
}
