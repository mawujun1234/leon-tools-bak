package test.mawujun.system;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.mawujun.system.JavaInfo;
import com.mawujun.system.SystemUtil;

public class SystemUtilTest {
	
	@Test
	@Ignore
	public void dumpTest() {
		SystemUtil.dumpSystemInfo();
	}
	
	@Test
	public void getCurrentPidTest() {
		long pid = SystemUtil.getCurrentPID();
		Assert.assertTrue(pid > 0);
	}
	
	@Test
	public void getJavaInfoTest() {
		JavaInfo javaInfo = SystemUtil.getJavaInfo();
		Assert.assertNotNull(javaInfo);
	}
	
}
