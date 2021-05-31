package com.mawujun.ssh;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

/**
 * scp的 demo程序，现在存在的问题是 密码如何输入，参考官方文件，弹出一个框进行输入？
 * @author mawujun
 *
 */
public class SSHBase {
	String host;
    String user;
    Session session;
    UserInfo ui;

    final Logger logger = LoggerFactory.getLogger(SSHBase.class);

    public SSHBase(String host, String user, String passwd) {
        this.host = host;
        this.user = user;
        this.ui = new MyUserInfo(passwd);
    }

    public void connect() {
        try {
            JSch jsch = new JSch();

            this.session = jsch.getSession(this.user, this.host, 22);
            this.session.setUserInfo(this.ui);
            this.session.connect();
            logger.info("connect to {}@{} success.", this.user, this.host);
        } catch (JSchException e) {
            logger.error("connect to {} failed.", this.host, e);
        }
    }


    public boolean scpToRemote(String srcFile, String dstFile) {
        Channel channel = null;
        try {
            FileInputStream fis = null;
            boolean ptimestamp = true;

            // exec 'scp -t rfile' remotely
            String command = "scp " + (ptimestamp ? "-p" : "") + " -t " + dstFile;
            channel = this.session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            // get I/O streams for remote scp
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();

            channel.connect();

            if (checkAck(in) != 0) {
                logger.error("scp {} check ack failed.", srcFile);
                return false;
            }

            File _lfile = new File(srcFile);

            if (ptimestamp) {
                command = "T " + (_lfile.lastModified() / 1000) + " 0";
                // The access time should be sent here,
                // but it is not accessible with JavaAPI ;-<
                command += (" " + (_lfile.lastModified() / 1000) + " 0\n");
                out.write(command.getBytes());
                out.flush();
                if (checkAck(in) != 0) {
                    logger.error("scp {} check ack failed.", srcFile);
                    return false;
                }
            }

            // send "C0644 filesize filename", where filename should not include '/'
            long filesize = _lfile.length();
            command = "C0644 " + filesize + " ";
            if (srcFile.lastIndexOf('/') > 0) {
                command += srcFile.substring(srcFile.lastIndexOf('/') + 1);
            } else {
                command += srcFile;
            }
            command += "\n";
            out.write(command.getBytes());
            out.flush();
            if (checkAck(in) != 0) {
                logger.error("scp {} check ack failed.", srcFile);
                return false;
            }

            // send a content of lfile
            fis = new FileInputStream(srcFile);
            byte[] buf = new byte[1024];
            while (true) {
                int len = fis.read(buf, 0, buf.length);
                if (len <= 0) break;
                out.write(buf, 0, len); //out.flush();
            }
            fis.close();
            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();
            if (checkAck(in) != 0) {
                logger.error("scp {} check ack failed.", srcFile);
                return false;
            }
            out.close();
            channel.disconnect();
        } catch (JSchException e) {
            logger.error("scp {} failed.", srcFile, e);
            return false;
        } catch (IOException e) {
            logger.error("scp {} failed.", srcFile, e);
            return false;
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
        return true;
    }

    public void close() {
        if (this.session != null) {
            this.session.disconnect();
        }
    }

    public class MyUserInfo implements UserInfo {
        String passwd;

        public MyUserInfo(String passwd) {
            this.passwd = passwd;
        }

        public String getPassword() {
            return passwd;
        }

        public boolean promptYesNo(String str) {
            return true;
        }

        public String getPassphrase() {
            return null;
        }

        public boolean promptPassphrase(String message) {
            return true;
        }

        public boolean promptPassword(String message) {
            return true;
        }

        public void showMessage(String message) {
            logger.info(message);
        }

    }

    int checkAck(InputStream in) throws IOException {
        int b = in.read();
        // b may be 0 for success,
        //          1 for error,
        //          2 for fatal error,
        //          -1
        if (b == 0) return b;
        if (b == -1) return b;

        if (b == 1 || b == 2) {
            StringBuffer sb = new StringBuffer();
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            }
            while (c != '\n');
            if (b == 1) { // error
                logger.error(sb.toString());
            }
            if (b == 2) { // fatal error
                logger.error(sb.toString());
            }
        }
        return b;
    }
}
