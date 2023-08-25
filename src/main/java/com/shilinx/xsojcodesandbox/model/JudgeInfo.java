package com.shilinx.xsojcodesandbox.model;

import lombok.Data;

/**
 * 程序执行返回信息
 * @author slx
 */
@Data
public class JudgeInfo {
    /**
     * 程序执行信息
     */
    private String message;

    /**
     * 程序执行时间
     */
    private Long time;

    /**
     * 程序消耗内存
     */
    private Long memory;
}
