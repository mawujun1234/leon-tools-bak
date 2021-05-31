package com.mawujun.ssh;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.mawujun.system.SystemUtil;
import com.mawujun.util.ArrayUtil;

/**
 * 使用ssh连接linux服务器，同时长传，执行命令 注意不是线程安全的，会覆盖前面的配置
 * 
 * @author mawujun
 *
 */
public class SSHUtil {

//	public static void main(String[] args) throws Exception {
//		SSHUtil uploadTest = SSHUtil.connect("root", "123", "10.40.10.17", 52766);
//		uploadTest.uploadFile("/tmp/" + "readme-" + System.currentTimeMillis() + ".txt",
//				"C:\\Users\\xx\\Desktop\\sample\\readme.txt");
//		uploadTest.downloadFile("/tmp/readme.txt",
//				"C:\\Users\\xx\\Desktop\\sample\\readme-" + System.currentTimeMillis() + ".txt");
//	}
//
//	
	// https://www.cnblogs.com/yougewe/p/9580350.html
	// https://blog.csdn.net/gravely/article/details/80937550
	// http://www.jcraft.com/jsch/examples/Exec.java.html
	private static Logger logger = LoggerFactory.getLogger(SSHUtil.class);

	// private ChannelSftp sftp = null;

	private Session sshSession = null;

	private String username;

	private String password;

	private String host;

	private int port;

	// 默认session通道存活时间（我这里定义的是5分钟）
	private static int SESSION_TIMEOUT = 300000;
	// 默认connect通道存活时间
	private static int CONNECT_TIMEOUT = 3000;
	// 默认端口号
	private static int DEFULT_PORT = 22;

//	public SSHUtil(String username, String password, String host, int port) {
//		this.username = username;
//		this.password = password;
//		this.host = host;
//		this.port = port;
//	}

	public static SSHUtil connect(String username, String password, String host) {
		return SSHUtil.connect(username, password, host, DEFULT_PORT);
	}

	/**
	 * 连接sftp服务器，注意不是线程安全的 配置
	 *
	 * @return ChannelSftp sftp连接实例
	 */
	/**
	 * @param username
	 * @param password
	 * @param host
	 * @param port
	 * @return
	 */
	public static SSHUtil connect(String username, String password, String host, int port) {
		logger.info("-->sftp连接开始>>>>>> " + host + ":" + port + " >>>username=" + username);
		SSHUtil ssHUtil = new SSHUtil();
		ssHUtil.setUsername(username);
		ssHUtil.setPassword(password);
		ssHUtil.setHost(host);
		ssHUtil.setPort(port);
		;

		JSch jsch = new JSch();
		try {
			jsch.getSession(username, host, port);
			// sshSession = jsch.getSession(username, host, port);
			ssHUtil.setSshSession(jsch.getSession(username, host, port));

			// sshSession.setPassword(password);
			ssHUtil.getSshSession().setPassword(password);
			Properties properties = new Properties();
			properties.put("StrictHostKeyChecking", "no");
			ssHUtil.getSshSession().setConfig(properties);
			ssHUtil.getSshSession().connect(SESSION_TIMEOUT);
//			Channel channel = ssHUtil.getSshSession().openChannel("sftp");
//			//指定通道存活时间
//            channel.connect(CONNECT_TIMEOUT);
//
//			channel.connect();
//			//sftp = (ChannelSftp) channel;
//			ssHUtil.setSftp((ChannelSftp) channel);
			logger.info(" Connected to " + host + ":" + port);
		} catch (JSchException e) {
			throw new RuntimeException("sftp连接失败", e);
		}
		return ssHUtil;
	}

	public Session getSshSession() {
		if (sshSession == null) {
			sshSession = SSHUtil.connect(username, password, host, port).getSshSession();
		}
		return sshSession;
	}


	/**
	 * 执行命令,，通过之前我们获取的session打开这个通道，然后把命令放进去，通过getInputStream（）方法，获取一个输入流，这个输入流是用来读取该命令执行后，服务器的执行结果，比如：执行ls，那么服务器本身肯定会有反馈的，这里就是把这个反馈读出来。
	 * 
	 * 
	 * 但是你可以使用普通shell的分隔符（&，&&，|，||，; , \n, 复合命令）来提供多个命令。   一次性执行多条shell的方法：
	 * 
	 *         1）每个命令之间用;隔开           
	 *  说明：各命令的执行给果，不会影响其它命令的执行。换句话说，各个命令都会执行，但不保证每个命令都执行成功。
	 * 
	 *         2）每个命令之间用&&隔开           
	 *  说明：若前面的命令执行成功，才会去执行后面的命令。这样可以保证所有的命令执行完毕后，执行过程都是成功的。
	 * 
	 *         3）每个命令之间用||隔开           
	 *  说明：||是或的意思，只有前面的命令执行失败后才去执行下一条命令，直到执行成功一条命令为止。
	 * 
	 * @param cmd 要执行的命令
	 *            <ol>
	 *            比如：
	 *            <li>ls</li>
	 *            <li>cd opt/</li>
	 *            </ol>
	 *            <ol>
	 *            多个连续命令可用 && 连接
	 *            <li>cd /opt/softinstaller && chmod u+x *.sh && ./installArg.sh
	 *            java</li>
	 *            </ol>
	 * @return 成功执行返回true 连接因为错误异常断开返回false
	 */
	public String exec(String cmd) {
		logger.warn("执行系统命令：{}", cmd);
		ChannelExec channelExec = null;
		try {
			// 开启exec通道
			channelExec = (ChannelExec) this.getSshSession().openChannel("exec");
			if (channelExec == null) {
				logger.error("打开exec通道失败，需要执行的系统命令：{}", cmd);
			}
			// 你只能有调用setCommand()方法一次，多次调用只有最后一次生效
			channelExec.setCommand(cmd);
			channelExec.setInputStream(null);
			channelExec.setErrStream(System.err);
			// 获取服务器输出流
			InputStream in = channelExec.getInputStream();
			channelExec.connect(CONNECT_TIMEOUT);

			int res = -1;
			StringBuffer buf = new StringBuffer(1024);
			byte[] tmp = new byte[1024];
			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if (i < 0) {
						break;
					}
					buf.append(new String(tmp, 0, i));
				}
				if (channelExec.isClosed()) {
					res = channelExec.getExitStatus();
					break;
				}
				TimeUnit.MILLISECONDS.sleep(100);
			}
			logger.info("执行结果：{}", buf);
			if (res == 0) {
				return buf.toString();
			}
		} catch (JSchException e) {
			logger.error("开启通道失败", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("读取服务器命令结果失败", e);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("关闭通道失败", e);
		} finally {
			if (channelExec != null) {
				// 关闭通道
				channelExec.disconnect();
			}
		}

		return null;
	}

	public void exec(List<String> cmds) {
		this.exec(cmds.toArray(new String[cmds.size()]));
	}

	/**
	 * 执行多条命令
	 * 
	 * @param cmds
	 * @return
	 */
	public void exec(String... cmds) {
		// JSch jsch = new JSch();
		String result = "";

		// Channel channel = null;
		Session session = null;
		ChannelShell channelShell = null;
		try {
			session = this.getSshSession();

			/*
			 * channel = session.openChannel("shell"); String cmdsString = ""; for (String
			 * cmd : cmds) { cmdsString += cmd+" \r\n"; } System.out.println(cmdsString);
			 * InputStream is = new ByteArrayInputStream(cmdsString.getBytes());
			 * channel.setInputStream(is); ByteArrayOutputStream baos = new
			 * ByteArrayOutputStream(); channel.setOutputStream(baos);
			 * System.out.println("channel1:" + channel.isConnected());
			 * channel.connect(30000); TimeUnit.SECONDS.sleep(5); String tmpResult = new
			 * String(baos.toByteArray()); // channel.connect(30000); // result+=tmpResult;
			 * System.out.println(tmpResult);
			 * 
			 */

			channelShell = (ChannelShell) session.openChannel("shell");
			channelShell.connect(CONNECT_TIMEOUT);

			// channelShell.setPtyType("dumb");

			// 和用户进行交互，执行命令http://www.jcraft.com/jsch/examples/Shell.java.html
			// channel.setInputStream(System.in);

			InputStream inputStream = channelShell.getInputStream();
			OutputStream outputStream = channelShell.getOutputStream();

			// 否则会出现乱码一样的数据，那些是颜色数据， 类似于^[[01;34mfilename^[[00m，
			outputStream.write(("alias ls=\"ls --color=never\"" + " \r").getBytes());
			outputStream.flush();

			for (String cmd : cmds) {
				// logger.info("开始执行命令:"+cmd);
//				if(cmd.startsWith("ls")) {
//					cmd="ls --color=never";
//				}
				outputStream.write((cmd + " \r").getBytes());
				outputStream.flush();
				TimeUnit.SECONDS.sleep(2);
				if(cmd.startsWith("netstat")) {
					TimeUnit.SECONDS.sleep(10);
				}
				// BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
				// System.out.println(cmd);

				byte[] tmp = new byte[1024];
				StringBuffer buf = new StringBuffer();
				while (true) {
					while (inputStream.available() > 0) {
						int i = inputStream.read(tmp, 0, 1024);
						if (i < 0)
							break;

						String s = new String(tmp, 0, i);
						if (s.indexOf("--More--") >= 0) {
							outputStream.write((" ").getBytes());
							outputStream.flush();
						}

						buf.append(s);
					}
					TimeUnit.SECONDS.sleep(1);
					if (channelShell.isClosed()) {
						if (inputStream.available() > 0)
							continue;
						logger.info("exit-status: " + channelShell.getExitStatus());
						break;
					}
					break;
					// inputStream.close();
				}
				logger.info(buf.toString());
			}

		} catch (JSchException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (channelShell != null)
				channelShell.disconnect();
			if (session != null)
				session.disconnect();
		}
		// return result;
	}

	/**
	 * 下载单个文件，如果指定文件名，则下载到文件名否则保持原有文件名 结束后，别忘记调用disconnect()
	 * 
	 * @param remoteFilePath 远程文件路径 /tmp/xxx.txt 或目录：/tmp
	 * @param localFilePath  本地文件路径 如 D:\\xxx.txt 或目录：D:\\tmp
	 * @return 下载的文件
	 */
	public void downloadFile(String remoteFilePath, String localFilePath) {
		logger.info(">>>>>>>>>downloadFile--ftp下载文件" + remoteFilePath + "开始>>>>>>>>>>>>>");
		ChannelSftp sftp = null;
		Channel channel=null;
		try {
			channel = this.getSshSession().openChannel("sftp");
			channel.connect(CONNECT_TIMEOUT);
			sftp = (ChannelSftp) channel;
		} catch (JSchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("通道连接失败...", e);
		}
		
		download(sftp,channel,remoteFilePath,localFilePath);

//		String remoteFileName = "";
//		// 远端目录确定以 / 作为目录格式
//		String rFileSeparator = "/";
//		int rDirNameSepIndex = remoteFilePath.lastIndexOf(rFileSeparator) + 1;
//		String rDir = remoteFilePath.substring(0, rDirNameSepIndex);
//		remoteFileName = remoteFilePath.substring(rDirNameSepIndex);
//		if (localFilePath.endsWith(File.separator)) {
//			localFilePath = localFilePath
//					+ (localFilePath.endsWith(File.separator) ? remoteFileName : "/" + remoteFileName);
//		}
//		File file = null;
//		OutputStream output = null;
//
//		ChannelSftp sftp = null;
//		// sftp.setFilenameEncoding("UTF-8");
//
//		try {
//			Channel channel = this.getSshSession().openChannel("sftp");
//			// 指定通道存活时间
//			channel.connect(CONNECT_TIMEOUT);
//			sftp = (ChannelSftp) channel;
//
//			file = new File(localFilePath);
//			if (file.exists()) {
//				file.delete();
//			}
//			file.createNewFile();
//			sftp.cd(rDir);
//			output = new FileOutputStream(file);
//			sftp.get(remoteFileName, output);
//			logger.info("===DownloadFile:" + remoteFileName + " success from sftp.");
//		} catch (SftpException e) {
//			logger.error("ftp下载文件失败", e);
//		} catch (FileNotFoundException e) {
//			logger.error("本地目录异常，请检查" + file.getPath(), e);
//		} catch (IOException e) {
//			logger.error("创建本地文件失败" + file.getPath(), e);
//		} catch (JSchException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			logger.error("获取通道失败" + file.getPath(), e);
//		} finally {
//			if (output != null) {
//				try {
//					output.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//			// disconnect();
//			if (sftp != null) {
//				if (sftp.isConnected()) {
//					sftp.disconnect();
//					sftp = null;
//					logger.info("sftp 连接已关闭！");
//				}
//			}
//		}

		logger.info(">>>>>>>>>downloadFile--ftp下载文件结束>>>>>>>>>>>>>");
		//return file;
	}
	
	private void download(ChannelSftp sftp,Channel channel,String remoteFilePath,String localFilePath) {
		String remoteFileName = "";
		// 远端目录确定以 / 作为目录格式
		String rFileSeparator = "/";
		int rDirNameSepIndex = remoteFilePath.lastIndexOf(rFileSeparator) + 1;
		String rDir = remoteFilePath.substring(0, rDirNameSepIndex);
		remoteFileName = remoteFilePath.substring(rDirNameSepIndex);
		if (localFilePath.endsWith(File.separator)) {
			localFilePath = localFilePath
					+ (localFilePath.endsWith(File.separator) ? remoteFileName : "/" + remoteFileName);
		}
		File file = null;
		OutputStream output = null;
		try {
			file = new File(localFilePath);
			if (file.exists() && file.isFile()) {
				file.delete();
			}
			
			sftp.cd(rDir);

			SftpATTRS sftpATTRS=sftp.lstat(remoteFilePath);
			if(sftpATTRS.isDir()) {
				file.mkdirs();
				Vector vector=sftp.ls(remoteFilePath);
				Enumeration<LsEntry> elements = vector.elements();
		        while (elements.hasMoreElements()) {
		            
		            LsEntry lsEntry=(LsEntry)elements.nextElement();
		            if("..".equals(lsEntry.getFilename()) || ".".equals(lsEntry.getFilename())) {
		            	continue;
		            }
		            //System.out.println(lsEntry.getFilename());
		            //System.out.println("============"+lsEntry.getLongname());
		            //lsEntry.getAttrs()
		            //只支持linux；
		            download(sftp,channel,remoteFilePath+"/"+lsEntry.getFilename(),localFilePath+File.separator+lsEntry.getFilename());
		        }
			} else {
				file.createNewFile();
				output = new FileOutputStream(file);
				sftp.get(remoteFileName, output);
				logger.info("===DownloadFile:" + remoteFilePath + " success from sftp.");
			}	
		} catch (SftpException e) {
			logger.error("ftp下载文件失败", e);
		} catch (FileNotFoundException e) {
			logger.error("本地目录异常，请检查" + file.getPath(), e);
		} catch (IOException e) {
			logger.error("创建本地文件失败" + file.getPath(), e);
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 上传单个文件，如果指正下载文件名则使用，否则保留原有文件名 结束后，别忘记调用disconnect()
	 * 
	 * @param uploadFilePath 要上传的文件 如：D:\\test\\xxx.txt 或目录：  D:\\test
	 * @param remoteFilePath 远程文件路径 /tmp/xxx.txt ||xxx.txt.zip，或目录  /tmp 
	 * 
	 */
	public void uploadFile(String uploadFilePath, String remoteFilePath) {
		logger.info(" begin uploadFile from:" + uploadFilePath + ", to: " + remoteFilePath);
		ChannelSftp sftp = null;
		Channel channel=null;
		try {
			channel = this.getSshSession().openChannel("sftp");
			channel.connect(CONNECT_TIMEOUT);
			sftp = (ChannelSftp) channel;
		} catch (JSchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("通道连接失败...", e);
		}
		
		upload(sftp,channel,uploadFilePath,remoteFilePath);
//		FileInputStream in = null;
//		// connect();
//		String remoteFileName = "";
//		String remoteDir = remoteFilePath;
//		String localFileName = "";
//		// 远端目录确定以 / 作为目录格式
//		String rFileSeparator = "/";
//		if (remoteFilePath.endsWith(rFileSeparator)) {
//			localFileName = uploadFilePath.substring(uploadFilePath.lastIndexOf(File.separator) + 1);
//			remoteFileName = localFileName;
//		} else {
//			int fileNameDirSep = remoteFilePath.lastIndexOf(rFileSeparator) + 1;
//			remoteDir = remoteFilePath.substring(0, fileNameDirSep);
//			remoteFileName = remoteFilePath.substring(fileNameDirSep);
//		}
//		ChannelSftp sftp = null;
//
//		try {
//			Channel channel = this.getSshSession().openChannel("sftp");
//			// 指定通道存活时间
//			channel.connect(CONNECT_TIMEOUT);
//			sftp = (ChannelSftp) channel;
//
//			sftp.cd(remoteDir);
//		} catch (SftpException e) {
//			try {
//				sftp.mkdir(remoteDir);
//				sftp.cd(remoteDir);
//			} catch (SftpException e1) {
//				logger.error("ftp创建文件路径失败，路径为" + remoteDir);
//				throw new RuntimeException("ftp创建文件路径失败" + remoteDir);
//			}
//		} catch (JSchException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			logger.error("通道连接失败...", e);
//		}
//		
//		
//		
//		
//		File file = new File(uploadFilePath);
//		try {
//			in = new FileInputStream(file);
//			//sftp.put(uploadFilePath, remoteFilePath);
//			sftp.put(in, remoteFileName);
//		} catch (FileNotFoundException e) {
//			logger.error("文件不存在-->" + uploadFilePath);
//		} catch (SftpException e) {
//			logger.error("sftp异常-->", e);
//		} finally {
//			if (in != null) {
//				try {
//					in.close();
//				} catch (IOException e) {
//					logger.info("Close stream logger.error." + e.getMessage());
//				}
//			}
//			// disconnect();
//		}
		logger.info(">>>>>>>>>uploadFile--ftp上传文件结束>>>>>>>>>>>>>");
	}
	private void upload(ChannelSftp sftp,Channel channel,String uploadFilePath,String remoteFilePath){
		FileInputStream in = null;
		String remoteFileName = "";
		String remoteDir = remoteFilePath;
		String localFileName = "";
		// 远端目录确定以 / 作为目录格式
		String rFileSeparator = "/";
		if (remoteFilePath.endsWith(rFileSeparator)) {
			localFileName = uploadFilePath.substring(uploadFilePath.lastIndexOf(File.separator) + 1);
			remoteFileName = localFileName;
		} else {
			int fileNameDirSep = remoteFilePath.lastIndexOf(rFileSeparator) + 1;
			remoteDir = remoteFilePath.substring(0, fileNameDirSep);
			remoteFileName = remoteFilePath.substring(fileNameDirSep);
		}
		
		try {
			sftp.cd(remoteDir);
		} catch (SftpException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			try {
				logger.info("创建远程目录:"+remoteDir);
				sftp.mkdir(remoteDir);
				sftp.cd(remoteDir);
			} catch (SftpException e1) {
				logger.error("ftp创建文件路径失败，路径为" + remoteDir);
				throw new RuntimeException("ftp创建文件路径失败" + remoteDir);
			}
		}
		
		File file = new File(uploadFilePath);
		if(file.isDirectory()) {
			File[] files=file.listFiles();
			for(File child:files) {
				//只支持linux
				upload(sftp,channel,child.getAbsolutePath(),remoteFilePath+"/"+child.getAbsolutePath().substring(uploadFilePath.length()+1));
				//System.out.println(remoteFilePath+"/"+child.getAbsolutePath().substring(uploadFilePath.length()+1));
				//System.out.println("=============");
			}
			
		} else {
			try {
				logger.info(file.getAbsolutePath());
				in = new FileInputStream(file);
				//sftp.put(uploadFilePath, remoteFilePath);
				sftp.put(in, remoteFileName);
			} catch (FileNotFoundException e) {
				logger.error("文件不存在-->" + uploadFilePath);
			} catch (SftpException e) {
				logger.error("sftp异常-->", e);
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						logger.info("Close stream logger.error." + e.getMessage());
					}
				}
				// disconnect();
			}
		}
		
	}


	/**
	 * 关闭连接
	 */
	public void disconnect() {

		if (this.sshSession != null) {
			if (this.sshSession.isConnected()) {
				this.sshSession.disconnect();
				this.sshSession = null;
				logger.info("sshSession 连接已关闭！");
			}
		}
	}
	
	/**
	 * 对本地的webpack项目进行打包,默认执行npm run build
	 * @param projectPath 本地项目地址，支持window和mac
	 */
	public static void npmRunBuild(String projectPath) {
		npmRunBuild(projectPath,"npm run build");
	}
	/**
	 * 对本地的webpack项目进行打包
	 * @param projectPath 本地项目地址，支持window和mac
	 * @param cmd 命令
	 */
	public static void npmRunBuild(String projectPath,String npmcmd) {

		// 编译本地的项目
		// https://www.cnblogs.com/mingforyou/p/3551199.html window和linux下的命令执行区别
		//https://saluya.iteye.com/blog/1260347 标准输出和标准错误 输出阻塞
		//https://www.cnblogs.com/polly333/p/7832540.html
		//https://blog.csdn.net/PiNk233/article/details/90597918 查找node安装目录
		//https://www.cnblogs.com/tianyu-cj/p/6517115.html mac查找node安装目录
		// String command = "cd E:\\xiaochegnxu\\syrs\\syrs-vue ";
		try {
			boolean iswindow=SystemUtil.getOsInfo().isWindows();
			String[] envp =getEnv(iswindow);

			//System.out.println("项目中先要执行过npm install,再用这个工具进行编译上传。");
			System.out.println("开始webpack打包:");
			String[] cmd =null;
			// -c 意思是执行完成自动关闭，这里多条linux命令通过&&连接到一起
			//直接执行编译
			if(iswindow) {
				cmd = new String[]{ "cmd","/C", "cd "+projectPath+" && "+npmcmd };
			} else {
				cmd = new String[]{ "/bin/sh", "-c", "cd "+projectPath+" && "+npmcmd };
			}
			System.out.println("执行命令:"+ArrayUtil.toString(cmd));
			//判断是否安装了node
			//if(iswindow) {
				//cmd={"cmd","/C","where node"}; 
			//} else {
				//cmd={ "/bin/sh", "-c", "which node" };
			//}
			
			
			//直接执行脚本
			//Process process = Runtime.getRuntime().exec("sh /Users/mawujun/git/syrs/syrs-admin/document/aaa.sh",envp);

			Process process = Runtime.getRuntime().exec(cmd,envp);
			
			
			ConsoleThread errorGobbler = new ConsoleThread(process.getErrorStream(), "Error");  
			ConsoleThread outputGobbler = new ConsoleThread(process.getInputStream(), "Output");  
			errorGobbler.start();  
			outputGobbler.start();  


			int result = process.waitFor();
			//if (process.exitValue() != 0) {
			if (result != 0) {
				//System.out.println("命令执行失败!原因1：命令错误。2：是否安装了node。3：环境变量是否配置好了");
				throw new RuntimeException("打包时发生异常,命令执行失败!原因1：命令错误。2：是否安装了node。3：环境变量是否配置好了");
			}

			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("打包时发生异常",e);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("打包时发生异常",e);
		}
		System.out.println("结束webpack打包");
	}
	
	/**
	 * 获取环境变量数组
	 * @return
	 */
	private static String[] getEnv(boolean iswindow) {
		//boolean iswindow=SystemUtil.getOsInfo().isWindows();
		Map map = System.getenv();  
		//启动的时候，是不带环境变量的，所以需要添加环境变量
		String[] envp = new String[map.size()];
//		//启动的时候，是不带环境变量的，所以需要添加环境变量
//		String[] envp = new String[] {"PATH="+map.get("PATH").toString()+":/usr/local/bin/"};
		
		System.out.println("环境变量:");
		Iterator it = map.entrySet().iterator();  
		int i=0;
		while(it.hasNext())  
		{  
		    Entry entry = (Entry)it.next();  
		    if("PATH".equalsIgnoreCase(entry.getKey().toString())) {
		    	if(iswindow) {
		    		envp[i]=entry.getKey()+"="+entry.getValue();
		    	} else {
		    		envp[i]=entry.getKey()+"="+entry.getValue()+":/usr/local/bin/";
		    	}		    	
		    } else {
		    	envp[i]=entry.getKey()+"="+entry.getValue();
		    }
		    
		    System.out.println(envp[i]);
		    i++;
		    
		}  
		return envp;
	}
	
	public static void mvnClean(String projectPath,boolean detailinfo) {
		mvnInstall(projectPath,"mvn clean "+(detailinfo?" -X ":""));
	}
	/**
	 * 打包maven项目
	 * @param projectPath
	 */
	public static void mvnInstall(String projectPath,boolean detailinfo) {
		//mvnInstall(projectPath,"mvn clean install"+(detailinfo?" -X ":""));
		mvnInstall(projectPath,"mvn clean install"+(detailinfo?" -X ":""));
	}
	/**
	 * 打包maven项目
	 * @param projectPath
	 * @param npmcmd
	 */
	public static void mvnInstall(String projectPath,String npmcmd) {
		// 编译本地的项目
				// https://www.cnblogs.com/mingforyou/p/3551199.html window和linux下的命令执行区别
				//https://saluya.iteye.com/blog/1260347 标准输出和标准错误 输出阻塞
				//https://www.cnblogs.com/polly333/p/7832540.html
				//https://blog.csdn.net/PiNk233/article/details/90597918 查找node安装目录
				//https://www.cnblogs.com/tianyu-cj/p/6517115.html mac查找node安装目录
				// String command = "cd E:\\xiaochegnxu\\syrs\\syrs-vue ";
				try {
					boolean iswindow=SystemUtil.getOsInfo().isWindows();
					String[] envp =getEnv(iswindow);

					//System.out.println("项目中先要执行过npm install,再用这个工具进行编译上传。");
					System.out.println("开始maven打包:");
					String[] cmd =null;
					// -c 意思是执行完成自动关闭，这里多条linux命令通过&&连接到一起
					//直接执行编译
					if(iswindow) {
						cmd = new String[]{ "cmd","/C", "cd "+projectPath+" && "+npmcmd };
					} else {
						cmd = new String[]{ "/bin/sh", "-c", "cd "+projectPath+" && "+npmcmd };
					}
					System.out.println("执行命令:"+ArrayUtil.toString(cmd));
					//判断是否安装了node
					//if(iswindow) {
						//cmd={"cmd","/C","where node"}; 
					//} else {
						//cmd={ "/bin/sh", "-c", "which node" };
					//}
					
					
					//直接执行脚本
					//Process process = Runtime.getRuntime().exec("sh /Users/mawujun/git/syrs/syrs-admin/document/aaa.sh",envp);

					Process process = Runtime.getRuntime().exec(cmd,envp);
					
					
					ConsoleThread errorGobbler = new ConsoleThread(process.getErrorStream(), "Error");  
					ConsoleThread outputGobbler = new ConsoleThread(process.getInputStream(), "Output");  
					errorGobbler.start();  
					outputGobbler.start();  


					int result = process.waitFor();
					//if (process.exitValue() != 0) {
					if (result != 0) {
						//System.out.println("命令执行失败!原因1：命令错误。2：是否安装了node。3：环境变量是否配置好了");
						throw new RuntimeException("打包时发生异常,命令执行失败!原因1：命令错误。2：是否安装了node。3：环境变量是否配置好了");
					}

					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new RuntimeException("打包时发生异常",e);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new RuntimeException("打包时发生异常",e);
				}
				System.out.println("结束maven打包");
	}
	
	
//	public class MyUserInfo implements UserInfo {
//  String passwd;
//
//  public MyUserInfo(String passwd) {
//      this.passwd = passwd;
//  }
//
//  public String getPassword() {
//      return passwd;
//  }
//
//  public boolean promptYesNo(String str) {
//      return true;
//  }
//
//  public String getPassphrase() {
//      return null;
//  }
//
//  public boolean promptPassphrase(String message) {
//      return true;
//  }
//
//  public boolean promptPassword(String message) {
//      return true;
//  }
//
//  public void showMessage(String message) {
//      logger.info(message);
//  }
//
//}
//public void scpTo(String local_file, String remote_file) {
//	if(SystemUtil.getOsInfo().isWindows()) {
//		throw new BizException("不支持window");
//	}
//	logger.info(">>>>>>>>>scp上传文件" + local_file + "开始>>>>>>>>>>>>>");
//	// http://www.jcraft.com/jsch/examples/ScpTo.java.html
//	// 也可以执行scp，只要使用命令
//	// scp://${remote-username}:${remote-passwd}@${remote-addr}${service-path}
//	FileInputStream fis = null;
//	try {
//
////		String local_file = arg[0];
////		String user = arg[1].substring(0, arg[1].indexOf('@'));
////		arg[1] = arg[1].substring(arg[1].indexOf('@') + 1);
////		String host = arg[1].substring(0, arg[1].indexOf(':'));
////		String remote_file = arg[1].substring(arg[1].indexOf(':') + 1);
//
//		// JSch jsch = new JSch();
//		//Session session = this.getSshSession();
//		
//		JSch jsch = new JSch();
//		
//		MyUserInfo ui=new MyUserInfo(password);
//
//		Session session = jsch.getSession(this.username, this.host, 22);
//      session.setUserInfo(ui);
//      session.connect();
//		
//
////		// username and password will be given via UserInfo interface.
////		UserInfo ui = new MyUserInfo();
////		session.setUserInfo(ui);
////		session.connect();
//
//		boolean ptimestamp = true;
//
////		// exec 'scp -t remote_file' remotely
////		remote_file = remote_file.replace("'", "'\"'\"'");
////		remote_file = "'" + remote_file + "'";
//		String command = "scp " + (ptimestamp ? "-p" : "") + " -t " + remote_file;
//
//		// scp local_file remote_username@remote_ip:remote_folder
//		// -p 保留原文件的修改时间，访问时间和访问权限。
//		//String command = "scp -r  -p " + local_file + " " + this.username + "@" + this.host + ":" + remote_file;
//		Channel channel = session.openChannel("exec");
//		((ChannelExec) channel).setCommand(command);
//		
//		//Channel channel=session.openChannel("shell");
//
//		// get I/O streams for remote scp
//		OutputStream out = channel.getOutputStream();
//		InputStream in = channel.getInputStream();
//
//		channel.connect(CONNECT_TIMEOUT);
//		
////		out.write((command + " \r").getBytes());
////		out.write((this.getPassword() + " \r").getBytes());
////		out.flush();
//
//		if (checkAck(in) != 0) {
//			System.exit(0);
//		}
//
//		File _lfile = new File(local_file);
//
//		if (ptimestamp) {
//			command = "T " + (_lfile.lastModified() / 1000) + " 0";
//			// The access time should be sent here,
//			// but it is not accessible with JavaAPI ;-<
//			command += (" " + (_lfile.lastModified() / 1000) + " 0\n");
//			out.write(command.getBytes());
//			out.flush();
//			if (checkAck(in) != 0) {
//				System.exit(0);
//			}
//		}
//
//		// send "C0644 filesize filename", where filename should not include '/'
//		long filesize = _lfile.length();
//		command = "C0644 " + filesize + " ";
//		if (local_file.lastIndexOf('/') > 0) {
//			command += local_file.substring(local_file.lastIndexOf('/') + 1);
//		} else {
//			command += local_file;
//		}
//		command += "\n";
//		out.write(command.getBytes());
//		out.flush();
//		if (checkAck(in) != 0) {
//			System.exit(0);
//		}
//
//		// send a content of local_file
//		fis = new FileInputStream(local_file);
//		byte[] buf = new byte[1024];
//		while (true) {
//			int len = fis.read(buf, 0, buf.length);
//			if (len <= 0)
//				break;
//			out.write(buf, 0, len); // out.flush();
//		}
//		fis.close();
//		fis = null;
//		// send '\0'
//		buf[0] = 0;
//		out.write(buf, 0, 1);
//		out.flush();
//		if (checkAck(in) != 0) {
//			System.exit(0);
//		}
//		out.close();
//
//		channel.disconnect();
//		// session.disconnect();
//
//		System.exit(0);
//	} catch (Exception e) {
//		System.out.println(e);
//		try {
//			if (fis != null)
//				fis.close();
//		} catch (Exception ee) {
//		}
//	}
//
//	logger.info(">>>>>>>>>scp上传文件到" + remote_file + "结束>>>>>>>>>>>>>");
//
//}
//
//int checkAck(InputStream in) throws IOException {
//	int b = in.read();
//	// b may be 0 for success,
//	// 1 for error,
//	// 2 for fatal error,
//	// -1
//	if (b == 0)
//		return b;
//	if (b == -1)
//		return b;
//
//	if (b == 1 || b == 2) {
//		StringBuffer sb = new StringBuffer();
//		int c;
//		do {
//			c = in.read();
//			sb.append((char) c);
//		} while (c != '\n');
//		if (b == 1) { // error
//			System.out.print(sb.toString());
//		}
//		if (b == 2) { // fatal error
//			System.out.print(sb.toString());
//		}
//	}
//	return b;
//}
//
///**
//* 从远程服务器拷贝文件下来,不支持window
//* 
//* @param remote_file
//* @param local_file
//*/
//public void scpFrom(String remote_file, String local_file) {
//	if(SystemUtil.getOsInfo().isWindows()) {
//		throw new BizException("不支持window");
//	}
//	logger.info(">>>>>>>>>scp下载文件" + remote_file + "开始>>>>>>>>>>>>>");
//	// http://www.jcraft.com/jsch/examples/ScpFrom.java.html
//	FileOutputStream fos = null;
//	try {
//
////    String user=arg[0].substring(0, arg[0].indexOf('@'));
////    arg[0]=arg[0].substring(arg[0].indexOf('@')+1);
////    String host=arg[0].substring(0, arg[0].indexOf(':'));
////    String remote_file=arg[0].substring(arg[0].indexOf(':')+1);
////    String local_file=arg[1];
//
//		String prefix = null;
//		if (new File(local_file).isDirectory()) {
//			prefix = local_file + File.separator;
//		}
//
////    JSch jsch=new JSch();
////    Session session=jsch.getSession(user, host, 22);
////
////    // username and password will be given via UserInfo interface.
////    UserInfo ui=new MyUserInfo();
////    session.setUserInfo(ui);
////    session.connect();
//
//		Session session = this.getSshSession();
//
//		// exec 'scp -f remote_file' remotely
//		remote_file = remote_file.replace("'", "'\"'\"'");
//		remote_file = "'" + remote_file + "'";
//		//String command = "scp -f " + remote_file;
//		String command = "scp -r " + this.username + "@" + this.host + ":" + remote_file  + " " + local_file;
//		Channel channel = session.openChannel("exec");
//		((ChannelExec) channel).setCommand(command);
//
//		// get I/O streams for remote scp
//		OutputStream out = channel.getOutputStream();
//		InputStream in = channel.getInputStream();
//
//		channel.connect(CONNECT_TIMEOUT);
//
//		byte[] buf = new byte[1024];
//
//		// send '\0'
//		buf[0] = 0;
//		out.write(buf, 0, 1);
//		out.flush();
//
//		while (true) {
//			int c = checkAck(in);
//			if (c != 'C') {
//				break;
//			}
//
//			// read '0644 '
//			in.read(buf, 0, 5);
//
//			long filesize = 0L;
//			while (true) {
//				if (in.read(buf, 0, 1) < 0) {
//					// error
//					break;
//				}
//				if (buf[0] == ' ')
//					break;
//				filesize = filesize * 10L + (long) (buf[0] - '0');
//			}
//
//			String file = null;
//			for (int i = 0;; i++) {
//				in.read(buf, i, 1);
//				if (buf[i] == (byte) 0x0a) {
//					file = new String(buf, 0, i);
//					break;
//				}
//			}
//
//			// System.out.println("filesize="+filesize+", file="+file);
//
//			// send '\0'
//			buf[0] = 0;
//			out.write(buf, 0, 1);
//			out.flush();
//
//			// read a content of local_file
//			fos = new FileOutputStream(prefix == null ? local_file : prefix + file);
//			int foo;
//			while (true) {
//				if (buf.length < filesize)
//					foo = buf.length;
//				else
//					foo = (int) filesize;
//				foo = in.read(buf, 0, foo);
//				if (foo < 0) {
//					// error
//					break;
//				}
//				fos.write(buf, 0, foo);
//				filesize -= foo;
//				if (filesize == 0L)
//					break;
//			}
//			fos.close();
//			fos = null;
//
//			if (checkAck(in) != 0) {
//				System.exit(0);
//			}
//
//			// send '\0'
//			buf[0] = 0;
//			out.write(buf, 0, 1);
//			out.flush();
//		}
//
//		session.disconnect();
//
//		System.exit(0);
//	} catch (Exception e) {
//		System.out.println(e);
//		try {
//			if (fos != null)
//				fos.close();
//		} catch (Exception ee) {
//		}
//	}
//	logger.info(">>>>>>>>>scp下载文件到" + local_file + "结束>>>>>>>>>>>>>");
//}


	public void setSshSession(Session sshSession) {
		this.sshSession = sshSession;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
