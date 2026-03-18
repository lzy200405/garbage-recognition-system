package edu.hubu.grs.service;

import edu.hubu.grs.entity.RecognitionRecord;
import org.springframework.web.multipart.MultipartFile;

public interface RecognitionService{

    /**
     *识别垃圾图形类型
     * @param file  图像文件
     * @param userId    查询的用户id
     * @return  识别记录类型
     */
    RecognitionRecord recognize(MultipartFile file,Long userId);
}
