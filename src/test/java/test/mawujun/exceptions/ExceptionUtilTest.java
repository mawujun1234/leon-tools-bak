package test.mawujun.exceptions;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.mawujun.exception.ExceptionUtil;
import com.mawujun.io.IORuntimeException;

/**
 * 异常工具单元测试
 *
 * @author looly
 */
public class ExceptionUtilTest {

	@Test
	public void wrapTest() {
		IORuntimeException e = ExceptionUtil.wrap(new IOException(), IORuntimeException.class);
		Assert.assertNotNull(e);
	}

	@Test
	public void getRootTest() {
		// 查找入口方法
		StackTraceElement ele = ExceptionUtil.getRootStackElement();
		Assert.assertEquals("main", ele.getMethodName());
	}

	@Test
	public void convertTest() {
		// RuntimeException e = new RuntimeException();
		IOException ioException = new IOException();
		IllegalArgumentException argumentException = new IllegalArgumentException(ioException);
		IOException ioException1 = ExceptionUtil.convertFromOrSuppressedThrowable(argumentException, IOException.class, true);
		Assert.assertNotNull(ioException1);
	}
}
