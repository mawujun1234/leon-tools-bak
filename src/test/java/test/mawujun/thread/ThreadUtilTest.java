package test.mawujun.thread;

import org.junit.Assert;
import org.junit.Test;

import com.mawujun.thread.ThreadUtil;

public class ThreadUtilTest {
	
	@Test
	public void executeTest() {
		final boolean isValid = true;
		
		ThreadUtil.execute(new Runnable() {
			
			@Override
			public void run() {
				Assert.assertTrue(isValid);
			}
		});
		
	}
}
