package test.mawujun.io.file;

import org.junit.Ignore;
import org.junit.Test;

import com.mawujun.io.FileUtil;
import com.mawujun.io.file.Tailer;
import com.mawujun.util.CharsetUtil;

public class TailerTest {
	
	@Test
	@Ignore
	public void tailTest() {
		FileUtil.tail(FileUtil.file("e:/tail.txt"), CharsetUtil.CHARSET_GBK);
	}
	
	@Test
	@Ignore
	public void tailWithLinesTest() {
		Tailer tailer = new Tailer(FileUtil.file("e:/tail.txt"), Tailer.CONSOLE_HANDLER, 2);
		tailer.start();
	}
}
