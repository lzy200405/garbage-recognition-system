package edu.hubu.grs.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Component
public class DeepSeekUtil {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekUtil.class);
    private static final String CHAT_COMPLETIONS_URL = "/v1/chat/completions";

    private static DeepSeekUtil instance;

    // 从 application.properties 读取配置
    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.api.base-url:https://api.deepseek.com}")
    private String baseUrl;

    @Value("${deepseek.model:deepseek-chat}")
    private String model;

    @Value("${deepseek.temperature:0.7}")
    private double temperature;

    @Value("${deepseek.max-tokens:2048}")
    private int maxTokens;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        instance = this;
        log.info("DeepSeekUtil initialized");
        if (apiKey == null || apiKey.isEmpty()) {
            log.error("警告：DeepSeek API Key 未配置！请在 application.properties 中设置 deepseek.api-key");
        } else {
            log.info("API Key 已配置: {}...", apiKey.substring(0, Math.min(10, apiKey.length())));
        }
    }

    private static DeepSeekUtil getInstance() {
        if (instance == null) {
            throw new IllegalStateException("DeepSeekUtil not initialized yet");
        }
        return instance;
    }

    /**
     * 带系统提示词的聊天方法
     * @param userMessage 用户消息
     * @param systemPrompt 系统提示词
     * @return AI回复内容
     */
    public static String chat(String userMessage, String systemPrompt) {
        return getInstance().doChat(userMessage, systemPrompt);
    }

    private String doChat(String userMessage, String systemPrompt) {
        // 检查API Key
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("sk-你的DeepSeek密钥")) {
            return "错误：请在 application.properties 中配置正确的 DeepSeek API Key";
        }

        String url = baseUrl + CHAT_COMPLETIONS_URL;

        try {
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            // 构建消息列表
            List<Map<String, String>> messages = new ArrayList<>();

            // 添加系统提示词
            if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
                Map<String, String> systemMsg = new HashMap<>();
                systemMsg.put("role", "system");
                systemMsg.put("content", systemPrompt);
                messages.add(systemMsg);
            }

            // 添加用户消息
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("temperature", temperature);
            requestBody.put("max_tokens", maxTokens);

            // 发送请求
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            log.debug("发送请求到 DeepSeek API");

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            // 解析响应
            return parseResponse(response.getBody());

        } catch (Exception e) {
            log.error("DeepSeek API 调用失败", e);
            return "调用失败: " + e.getMessage();
        }
    }

    private String parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).path("message");
                return message.path("content").asText();
            }
            return "无法解析响应";
        } catch (Exception e) {
            log.error("解析响应失败", e);
            return "解析响应失败: " + e.getMessage();
        }
    }
}