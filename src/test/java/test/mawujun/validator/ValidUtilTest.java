package test.mawujun.validator;

import org.junit.Test;
import org.junit.Assert;

import com.mawujun.regex.RegexUtil;
import com.mawujun.validator.ValidUtil;

public class ValidUtilTest {

	@Test
	public void isMobile() {
		Assert.assertTrue(ValidUtil.isMobile("15869365562"));
		Assert.assertFalse(ValidUtil.isMobile("1586936556"));
		Assert.assertTrue(ValidUtil.isMobile("17888249543"));
		Assert.assertTrue(ValidUtil.isMobile("19953134314"));

		Assert.assertTrue(ValidUtil.isMobile("19817290597"));
	}
	
	@Test
	public void isPhone() {
		Assert.assertTrue(ValidUtil.isTel("0575-3345219"));
		Assert.assertTrue(ValidUtil.isTel("0575-43452198"));
		//Assert.assertTrue(ValidUtil.isTel("05753345219"));
		Assert.assertTrue(ValidUtil.isTel("4345198"));
		
		Assert.assertFalse(ValidUtil.isTel("0575-434512198"));
		Assert.assertFalse(ValidUtil.isTel("1586936556"));
	}
	
	@Test
	public void isEmail() {
		Assert.assertTrue(ValidUtil.isEmail("mawujun@163.com"));
		Assert.assertTrue(ValidUtil.isEmail("1111@163.com"));
		Assert.assertFalse(ValidUtil.isEmail("1111@"));
	}
}
