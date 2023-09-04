package com.shilinx.xsojcodesandbox.controller;

import com.shilinx.xsojcodesandbox.JavaDockerCodeSendBoxOld;
import com.shilinx.xsojcodesandbox.JavaNativeCodeSandbox;
import com.shilinx.xsojcodesandbox.JavaNativeCodeSendBoxOld;
import com.shilinx.xsojcodesandbox.model.ExecuteCodeRequest;
import com.shilinx.xsojcodesandbox.model.ExecuteCodeResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author slx
 */
@RestController("/")
public class MainController {

    // 定义鉴权请求头和密钥
    private static final String AUTH_REQUEST_HEADER = "auth";

    private static final String AUTH_REQUEST_SECRET = "secretKey";


    @Resource
    JavaNativeCodeSandbox javaNativeCodeSendBox;

    @Resource
    JavaDockerCodeSendBoxOld javaDockerCodeSendBox;

    @PostMapping("/executeCode")
    public ExecuteCodeResponse executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest,
                                           HttpServletRequest request,
                                           HttpServletResponse response) {
        String authHeader = request.getHeader(AUTH_REQUEST_HEADER);
        if (!AUTH_REQUEST_SECRET.equals(authHeader)) {
            //403 状态表示无权限
            response.setStatus(403);
            return null;
        }
        if (executeCodeRequest == null) {
            throw new RuntimeException("参数为空");
        }
        return javaNativeCodeSendBox.executeCode(executeCodeRequest);
    }
}
