import java.io.FileDescriptor;

/**
 * 限制权限类
 * @author slx
 */
public class MySecurityManager extends SecurityManager{
    /**
     * 限制执行
     * @param cmd
     */
    @Override
    public void checkExec(String cmd) {
        //直接抛异常，中断程序就好了
        throw new SecurityException("权限异常:" + cmd);
    }

    @Override
    public void checkRead(FileDescriptor fd) {
//        super.checkRead(fd);
    }

    @Override
    public void checkWrite(String file) {
//        super.checkWrite(file);
    }

    @Override
    public void checkDelete(String file) {
//        super.checkDelete(file);
    }

    /**
     * 限制网路连接
     * @param host
     * @param port
     */
    @Override
    public void checkConnect(String host, int port) {
//        super.checkConnect(host, port);
    }
}