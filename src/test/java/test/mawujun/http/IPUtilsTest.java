package test.mawujun.http;

import org.junit.Test;

import com.mawujun.http.IPUtils;

public class IPUtilsTest {
	@Test
	public void test() {
		System.out.println("==="+IPUtils.getLocalIpAddr());
	}
	@Test
	public void test1() {
		System.out.println("获取ip组：");
		String[] aa=IPUtils.getLocalIpAddres();
		for(String ip:aa) {
			System.out.println("==="+ip);
		}
		
	}

}
