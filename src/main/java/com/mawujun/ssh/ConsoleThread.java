package com.mawujun.ssh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 异步输出标准输出和错误输出
 * @author mawujun
 *
 */
public class ConsoleThread extends Thread {
	InputStream is;
	String type;

	public ConsoleThread(InputStream is, String type) {
		this.is = is;
		this.type = type;
	}

	public void run() {
		InputStreamReader isr = null;
		BufferedReader br = null;
		try {
			isr = new InputStreamReader(is);
			br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				if (type.equals("Error")) {
					System.out.println("错误消息:" + line.replaceAll("\\x1b[[90m", "").replaceAll("\\x1b[[39m", ""));
				} else {
					System.out.println("正常消息:" + line.replaceAll("\\x1b\\[90m", "").replaceAll("\\x1b\\[39m", "")
							.replaceAll("\\x1b\\[22m", "").replaceAll("\\x1b\\[32m", "").replaceAll("\\x1b\\[1m", "").replaceAll("\\x1b\\[33m", ""));					
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {

			try {
				if (isr != null) {
					isr.close();
				}
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
}
