package edu.hubu.grs.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.hubu.grs.entity.RecognitionRecord;
import edu.hubu.grs.mapper.RecognitionRecordMapper;
import edu.hubu.grs.service.RecordService;
import edu.hubu.grs.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.object.UpdatableSqlQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class RecordServiceImpl extends ServiceImpl<RecognitionRecordMapper, RecognitionRecord> implements RecordService {

    @Autowired
    RedisUtil redisUtil;

    /**
     * 分页查询用户历史记录（带Redis缓存）
     */
    @Override
    public Page<RecognitionRecord> getUserRecordsByPage(Long userId, Integer pageNo, Integer pageSize) {

        String key = "record:page:" + userId + ":" + pageNo + ":" + pageSize;

        String cache = redisUtil.get(key);
        if (cache != null) {
            return JSON.parseObject(cache, Page.class);
        }

        Page<RecognitionRecord> page = new Page<>(pageNo, pageSize);

        QueryWrapper<RecognitionRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .orderByDesc("recognition_time");

        Page<RecognitionRecord> resultPage = this.page(page, wrapper);

        // 缓存10分钟
        redisUtil.set(key, JSON.toJSONString(resultPage), 600);

        return resultPage;
    }

    /**
     * 获取用户最近的记录
     */
    @Override
    public List<RecognitionRecord> getRecentRecords(Long userId, int limit) {
        QueryWrapper<RecognitionRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .orderByDesc("recognition_time")
                .last("LIMIT " + limit);

        return this.list(wrapper);
    }

    /**
     * 统计最近记录中出现次数最多的垃圾类型（带Redis缓存）
     */
    @Override
    public String getTopGarbageType(List<RecognitionRecord> records) {

        if (records == null || records.isEmpty()) {
            return null;
        }

        // 假设取第一条记录的userId作为key
        Long userId = records.get(0).getUserId();
        String key = "record:top:" + userId;

        String cache = redisUtil.get(key);
        if (cache != null) {
            return cache;
        }

        Map<String, Long> freqMap = records.stream()
                .collect(Collectors.groupingBy(RecognitionRecord::getRecognitionResult, Collectors.counting()));

        String topType = freqMap.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .get()
                .getKey();

        // 缓存5分钟
        redisUtil.set(key, topType, 300);

        return topType;
    }
}
