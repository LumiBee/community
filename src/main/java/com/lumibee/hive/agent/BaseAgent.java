package com.lumibee.hive.agent;

import cn.hutool.core.util.StrUtil;
import com.alibaba.dashscope.common.Role;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;


/**
 * 基础代理类，提供通用的代理执行逻辑
 * 子类需要实现具体的 step 方法来定义代理的行为
 */
@Data
@Log4j2
public abstract class BaseAgent {

    // 核心属性
    private String name;

    // 提示
    private String systemPrompt;
    private String nextStepPrompt;

    // 状态
    private AgentState state = AgentState.IDLE;

    // 执行控制
    private int maxSteps = 10;
    private int currentStep = 0;

    // LLM
    private ChatClient chatClient;

    // Memory(自主维护对话上下文)
    private List<Message> messageList = new ArrayList<>();

    // 循环检测
    private int duplicateThreshold = 2; // 重复检测阈值

    /**
     * 执行用户输入的提示
     * @param userPrompt 用户输入的提示
     * @return 执行结果
     */
    public String run(String userPrompt) {
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("Cannot run agent from this state: " + this.state);
        }
        if (StrUtil.isBlank(userPrompt)) {
            throw new RuntimeException("Cannot run agent with empty prompt");
        }

        // 设置状态为运行中
        this.state = AgentState.RUNNING;
        // 记录消息上下文
        messageList.add(new UserMessage(userPrompt));
        // 保存结果列表
        List<String> results = new ArrayList<> ();

        try {
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info("Executing step {}/{}", stepNumber, maxSteps);
                // 单步执行
                String stepResult = step();
                String result = "Step " + stepNumber + ": " + stepResult;
                results.add(result);
                // 检查是否卡住
                if (isStuck()) {
                    handleStuckState();
                }
            }
            // 检查是否达到最大步骤数
            if (currentStep >= maxSteps) {
                state = AgentState.FINISHED;
                results.add("Terminated: Reached max steps (" + maxSteps + ")");
            }
            return String.join("\n", results);
        } catch (Exception e) {
            log.error("Error running agent: {}", e.getMessage(), e);
            this.state = AgentState.ERROR;
            return "Error: " + e.getMessage();
        } finally {
            this.cleanup();
        }

    }

    /**
     * 执行下一步
     * @return 下一步的结果
     */
    public abstract String step();

    /**
     * 清理资源
     */
    protected void cleanup() {
        this.state = AgentState.IDLE;
        this.currentStep = 0;
        this.messageList.clear();
    }

    /**
     * 检查是否处于卡住状态
     * 如果连续两次执行结果相同，则认为代理卡住了
     */
    protected void handleStuckState() {
        String stuckPrompt = "监测到重复响应，考虑新策略，避免重复已经尝试过的无效路径";
        this.nextStepPrompt = stuckPrompt + "\n" + (this.nextStepPrompt != null ? this.nextStepPrompt : "");
        System.out.println("Agent is stuck, add next step prompt: " + stuckPrompt);
    }

    /**
     * 检查代理是否卡住
     * @return true 如果代理卡住了，false 否则
     */
    protected boolean isStuck() {
        List<Message> messages = this.messageList;
        if (messages.size() < 2) {
            return false; // 不足以判断是否卡住
        }

        Message lastMessage = messages.get(messages.size() - 1);
        if (lastMessage.getText() == null || lastMessage.getText().isEmpty()) {
            return false; // 最后一条消息为空，无法判断
        }

        // 计算重复内容出现的次数
        int duplicateCount = 0;
        for (int i = messages.size() - 2; i >= 0; i--) {
            Message msg = messages.get(i);
            if (msg.getMessageType() == MessageType.ASSISTANT && lastMessage.getText().equals(msg.getText())) {
                duplicateCount++;
            }
        }

        return duplicateCount >= this.duplicateThreshold;
    }
}
