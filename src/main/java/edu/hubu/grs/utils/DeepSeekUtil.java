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
import org.springframework.scheduling.annotation.Async;
import org.springframework.cache.annotation.Cacheable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DeepSeekUtil {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekUtil.class);
    private static final String CHAT_COMPLETIONS_URL = "/v1/chat/completions";

    // 缓存系统提示词和请求体模板
    private static final Map<String, List<Map<String, String>>> SYSTEM_PROMPT_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, Object>> REQUEST_BODY_TEMPLATE_CACHE = new ConcurrentHashMap<>();

    private static DeepSeekUtil instance;

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

    @Value("${deepseek.timeout:30000}")
    private int timeout; // 默认30秒超时

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        instance = this;
        log.info("DeepSeekUtil initialized with model: {}, baseUrl: {}", model, baseUrl);
        if (apiKey == null || apiKey.isEmpty()) {
            log.error("警告：DeepSeek API Key 未配置！请在 application.properties 中设置 deepseek.api-key");
        } else {
            log.info("API Key 已配置: {}...", apiKey.substring(0, Math.min(10, apiKey.length())));
        }

        // 预热缓存 - 预创建常用请求体模板
        preWarmCache();
    }

    /**
     * 预热缓存，减少首次请求时间
     */
    private void preWarmCache() {
        try {
            // 预创建请求体模板（不包含消息内容）
            Map<String, Object> template = new HashMap<>();
            template.put("model", model);
            template.put("temperature", temperature);
            template.put("max_tokens", maxTokens);
            REQUEST_BODY_TEMPLATE_CACHE.put("default", template);

            log.info("请求体模板缓存预热完成");
        } catch (Exception e) {
            log.warn("缓存预热失败", e);
        }
    }

    private static DeepSeekUtil getInstance() {
        if (instance == null) {
            throw new IllegalStateException("DeepSeekUtil not initialized yet");
        }
        return instance;
    }

    /**
     * 带系统提示词的聊天方法（同步）
     */
    public static String chat(String userMessage, String systemPrompt) {
        return getInstance().doChat(userMessage, systemPrompt);
    }

    /**
     * 异步聊天方法（不阻塞主线程）
     */
    @Async
    public static CompletableFuture<String> chatAsync(String userMessage, String systemPrompt) {
        return CompletableFuture.supplyAsync(() -> {
            return getInstance().doChat(userMessage, systemPrompt);
        });
    }

    /**
     * 核心聊天方法（优化版）
     */
    public String doChat(String userMessage, String systemPrompt) {
        // 检查API Key
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("sk-你的DeepSeek密钥")) {
            return "错误：请在 application.properties 中配置正确的 DeepSeek API Key";
        }

        String url = baseUrl + CHAT_COMPLETIONS_URL;

        try {
            // 1. 使用预热的请求体模板（减少对象创建开销）
            Map<String, Object> requestBody = new HashMap<>(REQUEST_BODY_TEMPLATE_CACHE.getOrDefault("default", new HashMap<>()));

            // 2. 使用缓存的消息列表（减少集合创建）
            List<Map<String, String>> messages = getCachedMessages(systemPrompt, userMessage);
            requestBody.put("messages", messages);

            log.debug("请求体构建完成，消息条数: {}", messages.size());

            // 3. 设置请求头（复用 HttpHeaders 实例？注意线程安全）
            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // 4. 发送请求（带超时控制）
            long startTime = System.currentTimeMillis();
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    String.class
            );
            long costTime = System.currentTimeMillis() - startTime;
            log.debug("DeepSeek API 调用完成，耗时: {}ms", costTime);

            System.out.println(response.getBody());

            // 5. 解析响应
            return parseResponse(response.getBody());

        } catch (Exception e) {
            log.error("DeepSeek API 调用失败", e);
            return "调用失败: " + e.getMessage();
        }
    }

    /**
     * 获取或创建缓存的消息列表
     */
    private List<Map<String, String>> getCachedMessages(String systemPrompt, String userMessage) {
        // 生成缓存key
        String cacheKey = systemPrompt != null ? systemPrompt.hashCode() + "_" + userMessage.hashCode() : "null_" + userMessage.hashCode();

        // 尝试从缓存获取
        if (SYSTEM_PROMPT_CACHE.containsKey(cacheKey)) {
            return SYSTEM_PROMPT_CACHE.get(cacheKey);
        }

        // 创建新的消息列表
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

        // 缓存结果（可以设置过期时间）
        if (messages.size() > 0) {
            SYSTEM_PROMPT_CACHE.put(cacheKey, messages);
        }

        return messages;
    }

    /**
     * 创建请求头（复用对象）
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setConnection("keep-alive"); // 保持连接
        return headers;
    }

    /**
     * 解析响应（优化版）
     */
    private String parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).path("message");
                return message.path("content").asText();
            }

            // 如果有错误信息
            JsonNode error = root.path("error");
            if (!error.isMissingNode()) {
                return "API错误: " + error.path("message").asText();
            }

            return "无法解析响应";
        } catch (Exception e) {
            log.error("解析响应失败", e);
            return "解析响应失败: " + e.getMessage();
        }
    }

}