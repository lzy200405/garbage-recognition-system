package edu.hubu.grs.service.impl;

import edu.hubu.grs.entity.RecognitionRecord;
import edu.hubu.grs.mapper.RecognitionRecordMapper;
import edu.hubu.grs.service.RecognitionService;
import edu.hubu.grs.utils.AliyunRecognitionUtil;
import edu.hubu.grs.utils.ImageUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;

@Service
public class RecognitionServiceImpl implements RecognitionService {

    @Autowired
    private RecognitionRecordMapper recordMapper;

    @Autowired
    private AliyunRecognitionUtil aliyunRecognitionUtil;

    @Autowired
    private ImageUploadUtil imageUploadUtil;


    @Override
    public RecognitionRecord recognize(MultipartFile file, Long userId) {
        RecognitionRecord record = new RecognitionRecord();
        try {
            // 1. 保存图片，获取输入流
            String imagePath = imageUploadUtil.upload(file, userId);
            FileInputStream inputStream = new FileInputStream(
                    imageUploadUtil.getFile(imagePath));
            // 2. 调用阿里云识别
            String result = aliyunRecognitionUtil.recognize(inputStream);

            // 3. 保存记录
            record.setUserId(userId);
            record.setImageUrl(imagePath);
            record.setRecognitionResult(result);
            record.setRecognitionTime(LocalDateTime.now());

            recordMapper.insert(record);//入数据库

        } catch (Exception e) {
            // 回滚事务并删除已保存图片
            if (record.getImageUrl() != null) {
                File fileToDelete = imageUploadUtil.getFile(record.getImageUrl());
                if (fileToDelete.exists()) {
                    fileToDelete.delete();
                }
            }

            // 区分异常类型
            String msg = e.getMessage().contains("图片") ? "图片上传失败" :
                    e.getMessage().contains("识别") ? "图像识别失败" :
                            "记录保存失败";
            throw new RuntimeException(msg, e);
        }
        return record;
    }
}
