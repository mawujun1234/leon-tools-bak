package test.mawujun.text.csv;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.mawujun.io.FileUtil;
import com.mawujun.lang.Assert;
import com.mawujun.text.csv.CsvData;
import com.mawujun.text.csv.CsvReader;
import com.mawujun.text.csv.CsvRow;
import com.mawujun.text.csv.CsvUtil;
import com.mawujun.text.csv.CsvWriter;
import com.mawujun.util.CharsetUtil;

public class CsvUtilTest {
	
	@Test
	public void readTest() {
		CsvReader reader = CsvUtil.getReader();
		//从文件中读取CSV数据
		CsvData data = reader.read(FileUtil.file("test.csv"));
		List<CsvRow> rows = data.getRows();
		for (CsvRow csvRow : rows) {
			Assert.notEmpty(csvRow.getRawList());
		}
	}
	
	@Test
	@Ignore
	public void writeTest() {
		CsvWriter writer = CsvUtil.getWriter("e:/testWrite.csv", CharsetUtil.CHARSET_UTF_8);
		writer.write(
				new String[] {"a1", "b1", "c1"}, 
				new String[] {"a2", "b2", "c2"}, 
				new String[] {"a3", "b3", "c3"}
		);
	}
	
}
