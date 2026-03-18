package edu.hubu.grs.service.impl;

import edu.hubu.grs.common.Config;
import edu.hubu.grs.entity.RecognitionRecord;
import edu.hubu.grs.service.KnowledgeService;
import edu.hubu.grs.service.RecordService;
import edu.hubu.grs.utils.DeepSeekUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgeServiceImpl implements KnowledgeService {

    @Autowired
    private RecordService recordService;

    @Override
    public String generateKnowledge(Long userId) {
        List<RecognitionRecord> userRecord = recordService.getRecentRecords(userId,Config.SEARCH_LIMIT);
        String GrabageType = recordService.getTopGarbageType(userRecord);
        String knowledge = DeepSeekUtil.chat(GrabageType, Config.DEEPSEEK_SYSTEMPROMPT);
        return knowledge;
    }
}
