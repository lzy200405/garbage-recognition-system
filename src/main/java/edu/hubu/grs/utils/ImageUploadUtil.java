package edu.hubu.grs.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class ImageUploadUtil {

    @Value("${file.upload.path}")
    private String uploadPath;

    /**
     * 上传图片，返回数据库存储的相对路径
     */
    public String upload(MultipartFile file, Long userId) throws IOException {

        // 1. 基础校验
        if (file == null || file.isEmpty()) {
            throw new IOException("文件为空");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("仅支持图片上传");
        }

        System.out.println("========== 开始上传文件 ==========");
        System.out.println("原始文件名: " + file.getOriginalFilename());
        System.out.println("文件大小: " + file.getSize() + " bytes");
        System.out.println("文件类型: " + file.getContentType());
        System.out.println("上传路径配置: " + uploadPath);

        // 2. 确保上传路径存在
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            boolean uploadDirCreated = uploadDir.mkdirs();
            System.out.println("创建上传根目录: " + uploadPath + " " + (uploadDirCreated ? "成功" : "失败"));
            if (!uploadDirCreated) {
                throw new IOException("无法创建上传根目录: " + uploadPath);
            }
        }

        // 3. 用户目录
        String userDirPath = uploadPath + File.separator + userId;
        File userDir = new File(userDirPath);
        if (!userDir.exists()) {
            boolean userDirCreated = userDir.mkdirs();
            System.out.println("创建用户目录: " + userDirPath + " " + (userDirCreated ? "成功" : "失败"));
            if (!userDirCreated) {
                throw new IOException("无法创建用户目录: " + userDirPath);
            }
        }

        // 4. 检查用户目录权限
        System.out.println("用户目录可读: " + userDir.canRead());
        System.out.println("用户目录可写: " + userDir.canWrite());
        System.out.println("用户目录可执行: " + userDir.canExecute());

        // 5. 文件名处理
        String originalName = file.getOriginalFilename();
        String suffix = "";

        if (originalName != null && originalName.contains(".")) {
            suffix = originalName.substring(originalName.lastIndexOf("."));
        } else {
            // 根据contentType推断后缀
            if (contentType != null) {
                if (contentType.contains("jpeg") || contentType.contains("jpg")) {
                    suffix = ".jpg";
                } else if (contentType.contains("png")) {
                    suffix = ".png";
                } else if (contentType.contains("gif")) {
                    suffix = ".gif";
                }
            }
        }

        String fileName = UUID.randomUUID() + suffix;
        System.out.println("生成文件名: " + fileName);

        // 6. 保存文件 - 使用 transferTo
        File dest = new File(userDir, fileName);
        System.out.println("目标文件路径: " + dest.getAbsolutePath());



        // 使用 Files.copy 作为备选
        System.out.println("尝试使用 Files.copy 备选方案");
        Path destPath = Paths.get(dest.getAbsolutePath());
        Files.copy(file.getInputStream(), destPath, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Files.copy 执行完成");


        // 8. 返回数据库路径
        String dbPath = userId + "/" + fileName;
        System.out.println("返回数据库路径: " + dbPath);
        System.out.println("========== 上传完成 ==========");

        return dbPath;
    }

    /**
     * 根据数据库路径获取本地文件
     */
    public File getFile(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            throw new RuntimeException("图片路径为空");
        }

        String fullPath = uploadPath + File.separator + imagePath;
        System.out.println("获取本地文件: " + fullPath);

        File file = new File(fullPath);
        System.out.println("文件是否存在: " + file.exists());
        if (file.exists()) {
            System.out.println("文件大小: " + file.length() + " bytes");
        }

        return file;
    }

    /**
     * 删除文件
     */
    public void delete(String imagePath) {
        File file = getFile(imagePath);
        if (file.exists()) {
            boolean deleted = file.delete();
            System.out.println("删除文件: " + file.getAbsolutePath() + " " + (deleted ? "成功" : "失败"));
        }
    }
}