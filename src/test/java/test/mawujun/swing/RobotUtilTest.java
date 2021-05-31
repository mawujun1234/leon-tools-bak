package test.mawujun.swing;

import org.junit.Ignore;
import org.junit.Test;

import com.mawujun.io.FileUtil;
import com.mawujun.swing.RobotUtil;

public class RobotUtilTest {

	@Test
	@Ignore
	public void captureScreenTest() {
		RobotUtil.captureScreen(FileUtil.file("e:/screen.jpg"));
	}
}
