package test.mawujun.lang;

import org.junit.Assert;
import org.junit.Test;

import com.mawujun.collection.CollUtil;
import com.mawujun.lang.WeightRandom;

public class WeightRandomTest {

	@Test
	public void weightRandomTest() {
		WeightRandom<String> random = WeightRandom.create();
		random.add("A", 10);
		random.add("B", 50);
		random.add("C", 100);

		String result = random.next();
		Assert.assertTrue(CollUtil.newArrayList("A", "B", "C").contains(result));
	}
}
