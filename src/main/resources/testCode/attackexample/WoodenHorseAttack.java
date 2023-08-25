import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * 植入木马文件并运行
 * @author slx
 */
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        //在你的系统中写恶意文件，并运行
        //获取目录
        final String userDir = System.getProperty("user.dir");
        String filePath = userDir + File.separator + "src/main/resources/木马.bat";
        //文件内容
        String whCmd = "java -version 2>&1";
        //创建文件
        Files.write(Paths.get(filePath), Arrays.asList(whCmd));
        //执行文件
        Process process = Runtime.getRuntime().exec(filePath);
        //获取一下执行结果
        process.waitFor();
        InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
        BufferedReader reader = new BufferedReader(inputStreamReader);
        while (reader.readLine() != null) {
            System.out.println(reader.readLine());
        }
        System.out.println("木马运行完毕");
    }
}