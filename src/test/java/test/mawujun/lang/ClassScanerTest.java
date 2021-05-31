package test.mawujun.lang;

import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import com.mawujun.lang.ClassScaner;
import com.mawujun.lang.Console;

public class ClassScanerTest {
	
	@Test
	@Ignore
	public void scanTest() {
		ClassScaner scaner = new ClassScaner("cn.hutool.core.util.StrUtil", null);
		Set<Class<?>> set = scaner.scan();
		for (Class<?> clazz : set) {
			Console.log(clazz.getName());
		}
	}
}
