package com.shilinx.xsojcodesandbox.model;

import lombok.Data;


/**
 * 进程执行输出信息类
 * @author slx
 */
@Data
public class ExecuteMessage {

    /**
     * 进程退出码
     * 0-正常
     */
    private Integer existValue;

    /**
     * 进程正常执行信息
     */
    private String message;

    /**
     * 异常执行信息
     */
    private String errorMessage;

    /**
     * 执行时间
     */
    private Long time;

    /**
     * 占用内存
     */
    private Long memory;
}
