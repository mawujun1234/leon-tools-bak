package test.mawujun.lang;

import org.junit.Assert;
import org.junit.Test;

import com.mawujun.date.DateTime;
import com.mawujun.lang.Dict;

public class DictTest {
	@Test
	public void dictTest(){
		Dict dict = Dict.create()
				.set("key1", 1)//int
				.set("key2", 1000L)//long
				.set("key3", DateTime.now());//Date
		
		Long v2 = dict.getLong("key2");
		Assert.assertEquals(Long.valueOf(1000L), v2);
	}
}
