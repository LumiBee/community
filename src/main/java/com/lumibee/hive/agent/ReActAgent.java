package com.lumibee.hive.agent;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class ReActAgent extends BaseAgent {

    /**
     * 代理的思考方法
     * 子类需要实现具体的思考逻辑
     *
     * @return 是否继续思考 true 表示继续思考，false 表示停止思考
     */
    public abstract boolean think();

    /**
     * 代理的行动方法
     * 子类需要实现具体的行动逻辑
     *
     * @return 行动结果
     */
    public abstract String act();

    @Override
    public String step() {
        try {
            boolean shouldAct = think();
            if (!shouldAct) {
                return "思考完成 - 不需要进一步行动";
            }
            return act();
        }catch (Exception e){
            e.printStackTrace();
            return "步骤执行失败: " + e.getMessage();
        }
    }
}