package com.mawujun.net;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 用于调用命令行获取可用端口
 */
public class SystemCommandUtil {
    private static final Logger LOG = LoggerFactory.getLogger(SystemCommandUtil.class);
//
//    public boolean isWindows() {
//        String osName = System.getProperty("os.name");
//        return osName.indexOf("Windows") != -1;
//    }

    /** 执行外部程序,并获取标准输出 */
    public String excuteCmdMultiThread(String[] cmd, String encoding) {
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(cmd);
            /* 为"错误输出流"单独开一个线程读取之,否则会造成标准输出流的阻塞 */
            Thread t = new Thread(new InputStreamRunnable(p.getErrorStream(), "ErrorStream"));
            t.start();
            /* "标准输出流"就在当前方法中读取 */
            String encodingStr = StringUtils.isEmpty(encoding) ? "UTF-8" : encoding;
            String string = IOUtils.toString(p.getInputStream(), encodingStr);
            return string;
        } catch (Exception e) {
            LOG.error("执行外部程序,并获取标准输出异常", e);
        } finally {
            p.destroy();
        }
        return null;
    }

    /** 读取InputStream的线程 */
    class InputStreamRunnable implements Runnable {
        BufferedReader bReader = null;
        String type = null;

        public InputStreamRunnable(InputStream is, String typeStr) {
            try {
                bReader = new BufferedReader(new InputStreamReader(new BufferedInputStream(is), "UTF-8"));
                type = typeStr;
            } catch (Exception ex) {
                LOG.error("读取InputStream的线程异常", ex);
            }
        }

        public void run() {
            String line = null;
            try {
                while ((line = bReader.readLine()) != null) {
                    LOG.error(line);
                }
                bReader.close();
            } catch (Exception ex) {
                LOG.error("", ex);
            }
        }
    }

}
