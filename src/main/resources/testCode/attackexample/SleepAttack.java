/**
 * 睡眠卡死攻击
 * @author slx
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        long ONE_HOUR = 60 * 60 * 1000;
        Thread.sleep(ONE_HOUR);
        //应该看不到
        System.out.println("睡完了");
    }
}