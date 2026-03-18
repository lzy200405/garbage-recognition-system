package edu.hubu.grs.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
public class ImageUploadUtil {

    @Value("${file.upload.path}")
    private String uploadPath;

    /**
     * 上传图片
     */
    public String upload(MultipartFile file, Long userId) throws IOException {

        // 1基础校验
        if (file == null || file.isEmpty()) {
            throw new IOException("文件为空");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("仅支持图片上传");
        }

        // 2️用户目录
        String userDirPath = uploadPath + File.separator + userId;
        File userDir = new File(userDirPath);
        if (!userDir.exists()) {
            userDir.mkdirs();
        }

        // 3️文件名处理
        String originalName = file.getOriginalFilename();
        String suffix = "";

        if (originalName != null && originalName.contains(".")) {
            suffix = originalName.substring(originalName.lastIndexOf("."));
        }

        String fileName = UUID.randomUUID() + suffix;

        // 4️保存文件
        File dest = new File(userDir, fileName);
        file.transferTo(dest);

        // 5️返回“数据库存储路径”（统一格式）
        return userId + "/" + fileName;
    }

    /**
     * 根据数据库路径获取本地文件
     */
    public File getFile(String imagePath) {

        if (imagePath == null || imagePath.isEmpty()) {
            throw new RuntimeException("图片路径为空");
        }


        String fullPath = uploadPath + File.separator + imagePath;

        return new File(fullPath);
    }

    /**
     * 删除文件（用于异常回滚）
     */
    public void delete(String imagePath) {

        File file = getFile(imagePath);
        if (file.exists()) {
            file.delete();
        }
    }
}