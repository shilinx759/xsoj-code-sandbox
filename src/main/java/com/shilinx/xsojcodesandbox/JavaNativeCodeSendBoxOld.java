package com.shilinx.xsojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.WordTree;
import com.shilinx.xsojcodesandbox.model.ExecuteCodeRequest;
import com.shilinx.xsojcodesandbox.model.ExecuteCodeResponse;
import com.shilinx.xsojcodesandbox.model.ExecuteMessage;
import com.shilinx.xsojcodesandbox.model.JudgeInfo;
import com.shilinx.xsojcodesandbox.utils.ProcessUtil;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * java 原生代码沙箱实现
 * @author slx
 */
@Service
public class JavaNativeCodeSendBoxOld implements CodeSendBox {

    public static final String GLOBAL_CODE_DIR_NAME = "tempCode";

    public static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

    public static final Long TIME_LIMIT = 5000L;

    public static final List<String> blackList = Arrays.asList("Files", "exec");

    public static final WordTree WORD_TREE;

    /**
     * 安全管理器编译文件父级目录的地址
     */
    public static final String SECURITY_MANAGER_PATH = "D:\\WorkSpace\\xsoj-code-sandbox\\src\\main\\resources\\security";

    /**
     * 安全管理器编译文件名
     */
    public static final String SECURITY_MANAGER_CLASS_NAME = "MySecurityManager";

    static {
        //初始化字典树
        WORD_TREE = new WordTree();
        WORD_TREE.addWords(blackList);
    }

    public static void main(String[] args) {
        //构造用户输入类
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Arrays.asList("1 2", "1 3"));
        //使用resource目录下的测试文件作为用户源码
        String code = ResourceUtil.readStr("testCode/simpleComputeArgs/Main.java", StandardCharsets.UTF_8);
//        String code = ResourceUtil.readStr("testCode/attackexample/WoodenHorseAttack.java", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage("java");
        //传入参数，创建用户源码文件
        JavaNativeCodeSendBoxOld javaNativeCodeSendBox = new JavaNativeCodeSendBoxOld();
        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSendBox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        //1.获取并保存用户输入的源码到指定位置
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();

        //使用字典树校验代码是否合法，如果code 中包含黑名单中的关键字，直接报错
/*        final FoundWord foundWord = WORD_TREE.matchWord(code);
        if (foundWord != null) {
            System.out.println("代码含有敏感词"+foundWord.getFoundWord());
            return null;
        }*/

        //检测是否有代码临时目录，没有则创建
        String userDir = System.getProperty("user.dir");
        String globalPathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
        if (!FileUtil.exist(globalPathName)) {
            FileUtil.mkdir(globalPathName);
        }
        //为每个单独的用户创建代码目录
        String userParentPath = globalPathName + File.separator + UUID.randomUUID();
        //拼接用户代码文件目录
        String userCodePath = userParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
        //创建文件
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);

        //2.编译用户的源码
        //拼接编译命令
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
        //执行命令，得到执行的程序
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            ExecuteMessage compileExecuteMessage = ProcessUtil.runProcessAndGetMessage(compileProcess, "编译");
            System.out.println(compileExecuteMessage);
        } catch (Exception e) {
            return getErrorResponse(e);
        }
        //3. 运行程序
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String inputArgs : inputList) {
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s;%s -Djava.security.manager=%s Main %s", userParentPath,SECURITY_MANAGER_PATH,SECURITY_MANAGER_CLASS_NAME, inputArgs);
            try {
                Process runProcess = Runtime.getRuntime().exec(runCmd);
/*                new Thread(()->{
                    try {
                        //先睡个超时时间
                        Thread.sleep(TIME_LIMIT);
                        //睡醒后杀死用户线程
                        runProcess.destroy();
                    } catch (InterruptedException e) {
                       throw new RuntimeException(e);
                    }
                }).start();*/
                ExecuteMessage runExecuteMessage = ProcessUtil.runProcessAndGetMessage(runProcess, "运行");
                System.out.println("runExecuteMessage = " + runExecuteMessage);
                executeMessageList.add(runExecuteMessage);
            } catch (Exception e) {
                return getErrorResponse(e);
            }
        }

        //4.整理执行信息
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        //拿到程序执行结果
        List<String> outputList = new ArrayList<>();
        //运行用例执行时间最大值
        long maxTime = 0;
        //检查是否有运行报错，有则设置返回状态为不通过
        for (ExecuteMessage executeMessage : executeMessageList) {
            String errorMessage = executeMessage.getErrorMessage();
            if (StrUtil.isNotBlank(errorMessage)) {
                executeCodeResponse.setMessage(errorMessage);
                //3-用户代码执行过程中存在错误
                executeCodeResponse.setStatus(3);
                break;
            }
            //只将正常的执行结果信息设置到返回结果集中
            outputList.add(errorMessage);
            Long time = executeMessage.getTime();
            //取输出中运行时间的最大值
            if (time != null) {
                maxTime = Math.max(maxTime, time);
            }
        }
        //只有当返回结果集和执行信息列表中数据大小一致时，表示执行结果没有错误信息
        if (outputList.size() == executeMessageList.size()) {
            executeCodeResponse.setStatus(1);
        }
        //返回输出结果
        executeCodeResponse.setOutputList(outputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        //设置为运行用例执行时间的最大值
        judgeInfo.setTime(maxTime);
        //judgeInfo.setMemory() 该操作在这种实现方法中操作非常麻烦，需要调用第三方库，所以不做实现
        //返回判题所需信息
        executeCodeResponse.setJudgeInfo(judgeInfo);

        //5.文件清理，清理掉tmpCode目录中每次执行完保存的用户的源码和编译文件
        //首先判断一下要删除的文件目录是否存在，处于安全考虑
        if (userCodeFile.getParentFile() != null) {
            boolean del = FileUtil.del(userParentPath);
            System.out.println("删除" + (del ? "成功" : "失败"));
        }
        //6.错误处理

        return executeCodeResponse;
    }

    /**
     * 获取错误响应
     * @param e
     * @return
     */
    private ExecuteCodeResponse getErrorResponse(Throwable e) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setMessage(e.getMessage());
        //代码沙箱系统错误
        executeCodeResponse.setStatus(2);
        executeCodeResponse.setJudgeInfo(new JudgeInfo());
        return executeCodeResponse;
    }
}
