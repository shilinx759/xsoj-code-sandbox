package com.shilinx.xsojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.shilinx.xsojcodesandbox.model.*;
import com.shilinx.xsojcodesandbox.utils.ProcessUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public abstract class JavaCodeSandboxTemplate implements CodeSendBox {
    public static final String GLOBAL_CODE_DIR_NAME = "tempCode";

    public static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

    private static final long TIME_OUT = 50000L;


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

    /**
     * 将用户提交的代码保存到一个临时目录下
     * @param code
     * @return
     */
    public File saveCodeToFile(String code) {

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

        return userCodeFile;
    }

    /**
     * 编译用户提交的代码，得到编译文件
     * @param userCodeFile
     * @return
     */
    public ExecuteMessage compileFile(File userCodeFile) {
        //拼接编译命令
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
        //执行命令，得到执行的程序
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            ExecuteMessage compileExecuteMessage = ProcessUtil.runProcessAndGetMessage(compileProcess, "编译");
            if (compileExecuteMessage.getExistValue() != 0) {
                compileExecuteMessage.setErrorMessage(JudgeInfoMessageEnum.COMPILE_ERROR.getValue());
//                throw new RuntimeException("用户代码编译错误");
                log.info("用户代码编译错误！");
                return compileExecuteMessage;
            }
            return compileExecuteMessage;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 执行编译后的文件，得到输出结果
     * @param userCodeFile 用户代码编译文件
     * @param inputList 本题输入样例
     * @return 每条输入样例的执行信息列表（消耗时间、内存、错误信息、退出码）
     */
    public List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList) {
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        File userParentPath = userCodeFile.getParentFile().getAbsoluteFile();
        for (String inputArgs : inputList) {
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s", userParentPath, inputArgs);
            try {
                Process runProcess = Runtime.getRuntime().exec(runCmd);
                // 超时控制
                new Thread(() -> {
                    try {
                        Thread.sleep(TIME_OUT);
                        System.out.println("超时了，中断");
                        runProcess.destroy();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
                ExecuteMessage runExecuteMessage = ProcessUtil.runProcessAndGetMessage(runProcess, "运行");
                System.out.println("runExecuteMessage = " + runExecuteMessage);
                executeMessageList.add(runExecuteMessage);
            } catch (Exception e) {
                throw new RuntimeException("用户代码执行错误",e);
            }
        }
        return executeMessageList;
    }

    /**
     * 分析程序执行结果，拆分得到用户代码的执行结果
     * @param executeMessageList
     * @return
     */
    public ExecuteCodeResponse getOutputResponse(List<ExecuteMessage> executeMessageList) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        JudgeInfo judgeInfo = new JudgeInfo();
        if (executeMessageList == null) {
            return executeCodeResponse;
        }
        //拿到程序执行结果
        List<String> outputList = new ArrayList<>();
        //运行用例执行时间最大值
        long maxTime = 0;
        //检查是否有运行报错，有则设置返回状态为不通过
        for (ExecuteMessage executeMessage : executeMessageList) {
            String errorMessage = executeMessage.getErrorMessage();
            if (StrUtil.isNotBlank(errorMessage)) {
                executeCodeResponse.setMessage(errorMessage);
                judgeInfo.setMessage(errorMessage);
                //3-用户代码执行过程中存在错误
                executeCodeResponse.setStatus(3);
                break;
            }
            //只将正常的执行结果信息设置到返回结果集中
            outputList.add(executeMessage.getMessage());
            Long time = executeMessage.getTime();
            //取输出中运行时间的最大值
            if (time != null) {
                maxTime = Math.max(maxTime, time);
            }
        }
        //只有当返回结果集和执行信息列表中数据大小不一致时，表示执行结果有错误信息
        if (outputList.size() != executeMessageList.size()) {
            executeCodeResponse.setStatus(3);
        }
        //执行成功
        executeCodeResponse.setStatus(2);
        judgeInfo.setMessage(JudgeInfoMessageEnum.ACCEPTED.getValue());
        //返回输出结果
        executeCodeResponse.setOutputList(outputList);

        //设置为运行用例执行时间的最大值
        judgeInfo.setTime(maxTime);
        judgeInfo.setMemory(0L); //该操作在这种实现方法中操作非常麻烦，需要调用第三方库，所以不做实现
        //返回判题所需信息
        executeCodeResponse.setJudgeInfo(judgeInfo);
        return executeCodeResponse;
    }

    /**
     * 用户程序执行完成后，将临时保存的用户代码和编译后文件的用户目录都删除
     * @param userCodeFile
     * @return
     */
    public boolean deleteFile(File userCodeFile) {
        File userParentPath = userCodeFile.getParentFile().getAbsoluteFile();
        if (userCodeFile.getParentFile() != null) {
            boolean del = FileUtil.del(userParentPath);
            System.out.println("删除" + (del ? "成功" : "失败"));
            return del;
        }
        //没有这个文件的话，就不用删，直接返回true
        return true;
    }

    /**
     * 执行代码总模板流程
     * @param executeCodeRequest
     * @return
     */
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {

        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();
        JudgeInfo judgeInfo = new JudgeInfo();


        //1.获取并保存用户输入的源码到指定位置
        File userCodeFile = saveCodeToFile(code);

        //2.编译用户的源码,得到 class 文件
        ExecuteMessage compileFileExecuteMessage = compileFile(userCodeFile);
        judgeInfo.setMessage(compileFileExecuteMessage.getErrorMessage());

        //3. 运行程序，输出结果
        if (compileFileExecuteMessage.getErrorMessage() != null) {
            return new ExecuteCodeResponse(null, JudgeInfoMessageEnum.COMPILE_ERROR.getValue(),QuestionSubmitStatusEnum.SUCCEED.getValue(), judgeInfo);
        }
        List<ExecuteMessage> executeMessageList = null;
        executeMessageList = runFile(userCodeFile, inputList);



        //4.整理执行信息
        ExecuteCodeResponse outputResponse = getOutputResponse(executeMessageList);
//        outputResponse.setJudgeInfo(judgeInfo);


        //5.文件清理，清理掉tmpCode目录中每次执行完保存的用户的源码和编译文件
        //首先判断一下要删除的文件目录是否存在，处于安全考虑
        boolean b = deleteFile(userCodeFile);
        if (!b) {
            log.error("deleteFile error, userCodeFilePath = {}", userCodeFile.getAbsoluteFile());
        }
        //6.错误处理

        return outputResponse;
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
