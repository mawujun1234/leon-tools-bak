package test.mawujun.text.csv;

import org.junit.Assert;
import org.junit.Test;

import com.mawujun.io.resource.ResourceUtil;
import com.mawujun.text.csv.CsvData;
import com.mawujun.text.csv.CsvReader;
import com.mawujun.util.CharsetUtil;

public class CsvReaderTest {
	
	@Test
	public void readTest() {
		CsvReader reader = new CsvReader();
		CsvData data = reader.read(ResourceUtil.getReader("test.csv", CharsetUtil.CHARSET_UTF_8));
		Assert.assertEquals("关注\"对象\"", data.getRow(0).get(2));
	}
}
