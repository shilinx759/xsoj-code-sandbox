package com.shilinx.xsojcodesandbox.utils;

import com.shilinx.xsojcodesandbox.model.ExecuteMessage;
import org.springframework.util.StopWatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 进程工具类
 * @author slx
 */
public class ProcessUtil {

    /**
     * 执行进程并返回执行结果信息
     * @param runProcess 执行的进程
     * @param opName 操作名
     * @return 执行结果信息
     */
    public static ExecuteMessage runProcessAndGetMessage(Process runProcess,String opName) {
        ExecuteMessage executeMessage = new ExecuteMessage();

        try {
            //监听执行时间，开始
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            //等带程序执行结束得到退出码，判断执行结果状态，0-正常结束
            int existValue = runProcess.waitFor();
            executeMessage.setExistValue(existValue);
            if (existValue == 0) {
                System.out.println(opName+"成功");
                //从终端的输入流中，分块获取终端的输出
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                StringBuilder compileOutputStringBuilder = new StringBuilder();
                //由于 bufferReader 是分批次的获取输入，所以这里判断是否有新的输出，有则继续读取
                String compileOutputLine;
                //逐行读取
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    compileOutputStringBuilder.append(compileOutputLine).append("\n");
                }
                //拼接完成后，整体输出
                System.out.println("compileOutputStringBuilder = " + compileOutputStringBuilder);
                executeMessage.setMessage(compileOutputStringBuilder.toString());
            } else {
                System.out.println(opName+"失败,退出码:" + existValue);
                //从终端的输入流中，分块获取终端的正常的输出
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                StringBuilder compileOutputStringBuilder = new StringBuilder();
                //由于 bufferReader 是分批次的获取输入，所以这里判断是否有新的输出，有则继续读取
                String compileOutputLine;
                //逐行读取
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    compileOutputStringBuilder.append(compileOutputLine).append("\n");
                }
                System.out.println("compileOutputStringBuilder = " + compileOutputStringBuilder);
                executeMessage.setMessage(compileOutputStringBuilder.toString());

                //获取异常输出
                //从终端的输入流中，分块获取终端的输出
                BufferedReader errorBufferReader = new BufferedReader(new InputStreamReader(runProcess.getErrorStream()));
                StringBuilder errorCompileOutputStringBuilder = new StringBuilder();

                //由于 bufferReader 是分批次的获取输入，所以这里判断是否有新的输出，有则继续读取
                String errorCompileOutputLine;
                //逐行读取
                while ((errorCompileOutputLine = errorBufferReader.readLine()) != null) {
                    errorCompileOutputStringBuilder.append(errorCompileOutputLine).append("\n");
                }
                System.out.println("errorCompileOutputStringBuilder = " + errorCompileOutputStringBuilder);
                executeMessage.setErrorMessage(errorCompileOutputStringBuilder.toString());
            }
            //计时结束
            stopWatch.stop();
            //得到执行的具体时间
            executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return executeMessage;
    }
}
