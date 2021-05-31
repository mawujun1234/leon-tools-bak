package test.mawujun.convert;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import com.mawujun.convert.Convert;

public class ConvertTest1 {
	SimpleDateFormat yyyyMMdd=new SimpleDateFormat("yyyy-MM-dd");
	SimpleDateFormat yyyyMMddHHmmss=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	SimpleDateFormat HHmmss=new SimpleDateFormat("HH:mm:ss");
	@Test
	public void test() {
		BigDecimal a=(BigDecimal)Convert.convert(BigDecimal.class,"1");
		assertEquals(new BigDecimal(1), a);
		
		Float b=(Float)Convert.convert(Float.class,"1.1");
		assertEquals(new Float(1.1), b);
		
		Object b1=Convert.convert( Float.class,"1.1");
		assertEquals(new Float(1.1), b1);
		
		Sex sex=(Sex)Convert.convert( Sex.class,"Man");
		assertEquals(Sex.Man, sex);
		
		//Date now=new Date();
		Date date=(Date)Convert.convert( Date.class,"2018-11-26");
		assertEquals("2018-11-26", yyyyMMdd.format(date));
		assertEquals("2018-11-26 00:00:00", yyyyMMddHHmmss.format(date));
		
		date=(Date)Convert.convert( Date.class,"2018-11-26 11:11:11");
		assertEquals("2018-11-26", yyyyMMdd.format(date));
		assertEquals("2018-11-26 11:11:11", yyyyMMddHHmmss.format(date));
		assertEquals("11:11:11", HHmmss.format(date));
		
		date=(Date)Convert.convert( Date.class,"11:11:11");
		assertEquals("11:11:11", HHmmss.format(date));
		
	}

}
