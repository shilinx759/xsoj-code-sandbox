package com.shilinx.xsojcodesandbox;

import com.shilinx.xsojcodesandbox.model.ExecuteCodeRequest;
import com.shilinx.xsojcodesandbox.model.ExecuteCodeResponse;

/**
 * 代码沙箱接口,方便拓展
 * @author slx
 */
public interface CodeSendBox {

    /**
     * 执行代码
     * @param executeCodeRequest
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
