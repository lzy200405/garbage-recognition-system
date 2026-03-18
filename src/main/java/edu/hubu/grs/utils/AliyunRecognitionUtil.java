package edu.hubu.grs.utils;

import com.aliyun.imagerecog20190930.Client;
import com.aliyun.imagerecog20190930.models.ClassifyingRubbishAdvanceRequest;
import com.aliyun.imagerecog20190930.models.ClassifyingRubbishResponse;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class AliyunRecognitionUtil {

    private static final String ACCESS_KEY_ID = "LTAI5tLAnDk9bpbkNdgDJ5xK";
    private static final String ACCESS_KEY_SECRET = "lsFpSMptZNLPVJUu0GIwsn3zE92xwg";

    /**
     * 初始化客户端
     */
    private Client createClient() throws Exception {
        Config config = new Config()
                .setAccessKeyId(ACCESS_KEY_ID)
                .setAccessKeySecret(ACCESS_KEY_SECRET);

        config.endpoint = "imagerecog.cn-shanghai.aliyuncs.com";
        return new Client(config);
    }

    /**
     * 本地文件流识别垃圾
     */
    public String recognize(InputStream inputStream) {

        try {
            Client client = createClient();

            ClassifyingRubbishAdvanceRequest request =
                    new ClassifyingRubbishAdvanceRequest()
                            .setImageURLObject(inputStream);

            RuntimeOptions runtime = new RuntimeOptions();

            ClassifyingRubbishResponse response =
                    client.classifyingRubbishAdvance(request, runtime);

            return parseResult(response);

        } catch (Exception e) {
            throw new RuntimeException("阿里云图像识别失败: " + e.getMessage());
        }
    }

    /**
     * 解析返回结果（核心）
     */
    private String parseResult(ClassifyingRubbishResponse response) {

        if (response == null || response.getBody() == null) {
            return "识别失败";
        }

        // 返回结果结构：
        // body.data.elements[0].category

        if (response.getBody().getData() != null &&
                response.getBody().getData().getElements() != null &&
                !response.getBody().getData().getElements().isEmpty()) {

            return response.getBody()
                    .getData()
                    .getElements()
                    .get(0)
                    .getCategory();   // 垃圾类别
        }

        return "未知垃圾";
    }
}