package com.shilinx.xsojcodesandbox.model;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 判题信息消息枚举
 * @author 86181
 */
public enum JudgeInfoMessageEnum {

    /**
     * 通过
     */
    ACCEPTED("Accepted", "通过"),
    /**
     * 等待中
     */
    WAITING("Waiting", "等待中"),
    /**
     * 错误答案
     */
    WRONG_ANSWER("Wrong Answer", "错误答案"),
     /**
     * 编译错误
     */
     COMPILE_ERROR("Compile Error", "编译错误"),
     /**
     * 时间超出限制
     */
     TIME_LIMIT_EXCEEDED("Time Limit Exceeded", "时间超出限制"),
     /**
     * 内存超出限制
     */
     MEMORY_LIMIT_EXCEEDED("Memory Limit Exceeded", "内存超出限制"),
     /**
     * 展示错误
     */
     PRESENTATION_ERROR("Presentation Error", "展示错误"),
     /**
     * 输出超限制
     */
     OUTPUT_LIMIT_EXCEEDED("Output Limit Exceeded", "输出超限制"),
    /**
     * 危险操作
     */
    DANGEROUS_OPERATION("Dangerous Operation", "危险操作"),
    /**
     * 运行时报错
     */
    RUNTIME_ERROR("Runtime Error", "运行时报错"),
    /**
     * 系统错误
     */
    SYSTEM_ERROR("System Error", "系统错误");





    private final String text;

    private final String value;

    JudgeInfoMessageEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static JudgeInfoMessageEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (JudgeInfoMessageEnum anEnum : JudgeInfoMessageEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
