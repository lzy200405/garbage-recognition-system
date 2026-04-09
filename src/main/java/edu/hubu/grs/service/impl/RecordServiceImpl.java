package edu.hubu.grs.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.hubu.grs.entity.RecognitionRecord;
import edu.hubu.grs.mapper.RecognitionRecordMapper;
import edu.hubu.grs.service.RecordService;
import edu.hubu.grs.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecordServiceImpl extends ServiceImpl<RecognitionRecordMapper, RecognitionRecord> implements RecordService {

    @Autowired
    private RedisUtil redisUtil;  // 改为 private

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

    /**
     * ========== 新增的缓存清除方法 ==========
     */

    /**
     * 新增识别记录（需要在你的Controller中调用这个方法）
     */
    public boolean addRecognitionRecord(RecognitionRecord record) {
        // 保存到数据库
        boolean result = this.save(record);
        if (result) {
            // 清除该用户的所有分页缓存
            clearUserPageCache(record.getUserId());
            // 清除该用户的top类型缓存
            clearUserTopCache(record.getUserId());
        }
        return result;
    }

    /**
     * 删除识别记录
     */
    public boolean deleteRecognitionRecord(Long recordId, Long userId) {
        boolean result = this.removeById(recordId);
        if (result) {
            // 清除该用户的所有分页缓存
            clearUserPageCache(userId);
            // 清除该用户的top类型缓存
            clearUserTopCache(userId);
        }
        return result;
    }

    /**
     * 清除用户的所有分页缓存
     */
    public void clearUserPageCache(Long userId) {
        // 删除该用户的所有分页缓存，格式如：record:page:123:1:10, record:page:123:2:10
        String pattern = "record:page:" + userId + ":*";
        redisUtil.deleteByPattern(pattern);
        System.out.println("已清除用户 " + userId + " 的所有分页缓存");
    }

    /**
     * 清除用户的top类型缓存
     */
    public void clearUserTopCache(Long userId) {
        String key = "record:top:" + userId;
        redisUtil.delete(key);
        System.out.println("已清除用户 " + userId + " 的top类型缓存");
    }

    /**
     * 获取当前用户的所有缓存键（用于调试）
     */
    public Set<String> getUserCacheKeys(Long userId) {
        String pattern = "record:*:" + userId + "*";
        return redisUtil.getKeys(pattern);
    }
}