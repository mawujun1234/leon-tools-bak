package test.mawujun.annotation;

import org.junit.Assert;
import org.junit.Test;

import com.mawujun.annotation.AnnotationUtil;

public class AnnotationUtilTest {
	
	@Test
	public void getAnnotationValueTest() {
		Object value = AnnotationUtil.getAnnotationValue(ClassWithAnnotation.class, AnnotationForTest.class);
		Assert.assertEquals("测试", value);
	}
	
	@AnnotationForTest("测试")
	class ClassWithAnnotation{
		
	}
}
