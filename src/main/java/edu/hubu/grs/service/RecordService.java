package edu.hubu.grs.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import edu.hubu.grs.entity.RecognitionRecord;

import java.util.List;
import java.util.Set;

public interface RecordService extends IService<RecognitionRecord> {


    /**
     * 通过用户id分页查询历史记录
     * @param userId    用户id
     * @param pageNo    页数
     * @param pageSize  页大小
     * @return          page类型
     */
    Page<RecognitionRecord> getUserRecordsByPage(Long userId, Integer pageNo, Integer pageSize);

    /**
     * 获得最近的记录
     * @param userId    用户ID
     * @param limit     条数限制
     * @return          识别记录类型
     */
    List<RecognitionRecord> getRecentRecords(Long userId, int limit);

    /**
     * 获得最多的垃圾类型
     * @param records   识别记录列表
     * @return
     */
    String getTopGarbageType(List<RecognitionRecord> records);

    /**
     * 清除用户的缓存
     * @param userId
     */
    void clearUserPageCache(Long userId);

    /**
     * 清除用户的最多缓存
     * @param userId
     */
    void clearUserTopCache(Long userId);
    }
