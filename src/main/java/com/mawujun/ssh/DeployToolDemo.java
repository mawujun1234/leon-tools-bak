package com.mawujun.ssh;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mawujun.io.FileUtil;
import com.mawujun.ssh.SSHUtil;
import com.mawujun.ssh.ConsoleThread;
import com.mawujun.system.SystemUtil;
import com.mawujun.util.ArrayUtil;

public class DeployToolDemo {
	static String root="root";
	static String password="xxxxxxxx";
	static String host="47.110.40.232";
	
	
	static String vue_projectPath;//vue项目根路径
	static String vue_removePath;//远程服务器部署的路径
	
	static String admin_projectPath;//= FileUtil.getProjectPath();
	static String admin_removePath;//远程服务器上存放的地址
	
	static String jarName = "syrs-admin.jar";//maven打包后的jar包地址

	public static void main(String[] args) {
		
		boolean iswindow=SystemUtil.getOsInfo().isWindows();
		if(iswindow) {
			vue_projectPath="E:\\xiaochegnxu\\syrs\\syrs-vue";
			admin_projectPath="E:\\xiaochegnxu\\syrs\\syrs-admin";
		} else {
			vue_projectPath="/Users/mawujun/git/syrs/syrs-vue";
			admin_projectPath="/Users/mawujun/git/syrs/syrs-admin";
		}
		vue_removePath="/data/www/dist";
		admin_removePath="/data/www/";
		
		
		//自动打包，上传打包好后的文件
		uploadVue();

		//自动打包，上传jar,并且重新启动
		uploadAdmin("prod");
	}


	public static void uploadVue() {
		//先自动打包
		SSHUtil.npmRunBuild(vue_projectPath);

		SSHUtil uploadTest = SSHUtil.connect(root, password, host, 22);

		// TODO Auto-generated method stub
		System.out.println(FileUtil.getProjectPath());
		String local_file = vue_projectPath+File.separator+"dist";//"E:\\xiaochegnxu\\syrs\\syrs-vue\\dist";
		//String remote_file = "/data/www/dist";
		uploadTest.uploadFile(local_file, vue_removePath);
		
		//是否重新启动nginx，
		
		uploadTest.disconnect();
	}

	public static void uploadAdmin(String profile) {
		//先自动打包
		SSHUtil.mvnInstall(admin_projectPath,false);
		
		// uploadTest.downloadFile(remote_file,"E:\\aaa.jar");
		SSHUtil uploadTest = SSHUtil.connect(root, password, host, 22);

		// TODO Auto-generated method stub
		System.out.println(FileUtil.getProjectPath());

		
		String local_file =admin_projectPath + File.separator + "target" + File.separator + jarName;
		String remote_file =  admin_removePath+ jarName;

		uploadTest.uploadFile(local_file, remote_file);

		List<String> cmds = new ArrayList<String>();
		// ps -ef |grep leon-fast.jar|grep -v "grep" |awk '{print $2}'
		cmds.add("kill -9 `ps -ef |grep " + jarName + "|grep -v \"grep\" |awk '{print $2}'`");
		// nohup java -jar ${service-path}/${pack-name} --spring.profiles.active=test >
		// ${service-path}/leon.log 2>&1 &
		cmds.add("nohup java -jar " + remote_file + " --spring.profiles.active="+profile+"  &");
		cmds.add("ps -ef | grep java | grep -v grep");
		cmds.add("netstat -nptl");
		uploadTest.exec(cmds);

		uploadTest.disconnect();
	}

}
