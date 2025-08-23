package com.lumibee.hive.agent;

/*
* 代理执行状态的枚举类
*/
public enum AgentState {
    /*
    * 空闲状态
    */
    IDLE,
    /*
    * 运行状态
    */
    RUNNING,
    /*
     * 结束状态
     */
    FINISHED,
    /*
     * 错误状态
     */
    ERROR
}
