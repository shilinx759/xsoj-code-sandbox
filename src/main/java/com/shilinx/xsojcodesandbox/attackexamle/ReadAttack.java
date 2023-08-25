package com.shilinx.xsojcodesandbox.attackexamle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * 读取配置文件敏感信息攻击
 * @author slx
 */
public class ReadAttack {
    public static void main(String[] args) throws IOException {
        String userDir = System.getProperty("user.dir");
        String filePath = userDir + File.separator + "src/main/resources/application.yml";
        //将文件路径创建为对象作为参数，去读取里面的内容
        List<String> allLines = Files.readAllLines(Paths.get(filePath));
        System.out.println(String.join("\n",allLines));;
    }
}
