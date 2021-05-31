package com.mawujun.net;

import com.mawujun.system.SystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.regex.Pattern;

/**
 * 自动模式下获取空闲端口
 */
public class PortUtil {
    private static final Logger LOG = LoggerFactory.getLogger(PortUtil.class);

    private static int maxRandomCount = 1000;

    private static int randomCount = 0;

    public static void resetCount(){
        randomCount = 0;
    }

    /**
     * 判断端口是否未被使用
     * @Title: isPortAvailable
     * @Description: 端口是否未被用
     * @param port
     * @return
     */
    public static boolean isPortAvailable(int port) {
        try {

            String[] commond = new String[2];
            commond[0] = "netstat";
            String encoding ="UTF-8";
            SystemCommandUtil systemCommandUtil = new SystemCommandUtil();
            if (SystemUtil.getOsInfo().isWindows()) {
                commond[1] = "-aon";
            } else {
                commond[1] = "-anp ";
                encoding = "utf-8";
            }
            String ret = systemCommandUtil.excuteCmdMultiThread(commond, encoding);
            boolean matches = Pattern.compile("(.+)("+port+"\\s+)(.*)").matcher(ret).find();
            return !matches;
        } catch (Exception e) {
            LOG.error("",e);
            return false;
        }
    }

    /**
     *
     * @Title: getRandomPort
     * @Description: 获取随机端口号
     * @param minPort
     * @param maxPort
     * @return
     */
    private static int getRandomPort(int minPort, int maxPort) {
        Random random = new Random();
        int s = random.nextInt(maxPort) % (maxPort - minPort + 1) + minPort;
        return s;
    }

    /**
     *
     * @Title: getUnAvailablePort
     * @Description:获取未被占用的随机端口号
     * @param minPort
     * @param maxPort
     * @return
     */
    public static int getUnAvailableRandomPort(int minPort, int maxPort) {
        if ((++randomCount) > maxRandomCount) {
            throw new RuntimeException("无法从" + minPort + "到" + maxPort + "绑定ehcache rmi同步端口号,请检查端口占用情况");
        }
        int randomPort = getRandomPort(minPort, maxPort);
        if (!isPortAvailable(randomPort)) {
            return getUnAvailableRandomPort(minPort, maxPort);
        }
        return randomPort;
    }


}
