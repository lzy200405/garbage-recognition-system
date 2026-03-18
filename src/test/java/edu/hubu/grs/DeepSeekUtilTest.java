package edu.hubu.grs;

import edu.hubu.grs.utils.DeepSeekUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DeepSeekUtilTest {

    @Test
    public void testChatWithSystemPrompt() {
        String userMessage = "写一首关于夏天的诗";
        String systemPrompt = "你是一个诗人，擅长写五言绝句";

        String result = DeepSeekUtil.chat(userMessage, systemPrompt);
        System.out.println("=== AI回复 ===");
        System.out.println(result);
    }

    @Test
    public void testGarbageClassification() {
        String result = DeepSeekUtil.chat(
                "有害垃圾",
                "你是一位专业的垃圾分类科普专家。请严格按照以下格式回答用户关于垃圾分类的问题。\n" +
                        "            \n" +
                        "            【回答格式要求】\n" +
                        "            1. 首先确认用户询问的垃圾种类\n" +
                        "            2. 按照以下三个部分进行回答：\n" +
                        "            \n" +
                        "            ========== 垃圾分类科普 ==========\n" +
                        "            \n" +
                        "            【分类标准】\n" +
                        "            • 垃圾类别：[请明确说明属于哪类垃圾 - 可回收物/有害垃圾/厨余垃圾/其他垃圾]\n" +
                        "            • 分类依据：[解释为什么属于这个类别]\n" +
                        "            \n" +
                        "            【详细科普】\n" +
                        "            • 定义：[简要说明这类垃圾的定义]\n" +
                        "            • 常见物品：[列举3-5个典型例子]\n" +
                        "            • 环境影响：[说明这类垃圾对环境的影响]\n" +
                        "            \n" +
                        "            【处理指南】\n" +
                        "            • 投放要求：[说明如何正确投放]\n" +
                        "            • 处理流程：[简要说明这类垃圾的处理过程]\n" +
                        "            • 注意事项：[列举2-3个重要注意事项]\n" +
                        "            \n" +
                        "            ========== 温馨提示 ==========\n" +
                        "            \n" +
                        "            \uD83D\uDC9A 垃圾分类，从我做起！如有疑问，欢迎继续咨询。\n" +
                        "            \n" +
                        "            【重要规则】\n" +
                        "            1. 回答必须包含上述所有部分\n" +
                        "            2. 语言要通俗易懂，适合普通市民理解\n" +
                        "            3. 信息要准确，符合中国现行垃圾分类标准\n" +
                        "            4. 鼓励积极环保，传播正能量\n" +
                        "            \"\"\";"
        );
        System.out.println("分类结果: " + result);
    }
}