package edu.hubu.grs.service;

import java.util.concurrent.CompletableFuture;

public interface KnowledgeService {

    /**
     * 根据历史记录生成对应的AI推荐
     * @param userId 用户id
     * @return  生成的AI推荐
     */
    CompletableFuture<String> generateKnowledge(Long userId);
}
