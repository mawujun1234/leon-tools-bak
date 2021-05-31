package test.mawujun.utils;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

import com.mawujun.utils.DateUtils;

import org.junit.Assert;

public class DateUtilsTest {

	@Test
	public void long2Date() {
		Date now=new Date();
		Date now_return=DateUtils.long2Date(now.getTime());
		Assert.assertEquals(now.getTime(), now_return.getTime());
	}
	
	@Test
	public void long2String() {
		Date now=new Date();
		String datetime=DateUtils.long2String(now.getTime());
		System.out.println(datetime);
		
		String datetime1=DateUtils.date2String(now);
		Assert.assertEquals(datetime, datetime1);
		
		Date now_return=DateUtils.string2Date(datetime, DateUtils.DATE_TIME);
		//Assert.assertEquals(now.getTime(), now_return.getTime());
		
	}
	
	@Test
	public void compareDate() {
		Date date1=DateUtils.string2Date("2018-10-24 20:20:20", DateUtils.DATE_TIME);
		Date date2=DateUtils.string2Date("2018-10-22 20:20:20", DateUtils.DATE_TIME);
		
		Assert.assertEquals(-2, DateUtils.compareDate(date1, date2, Calendar.DAY_OF_MONTH));
		Assert.assertEquals(2, DateUtils.compareDate(date2,date1,  Calendar.DAY_OF_MONTH));
		Assert.assertEquals(48, DateUtils.compareDate(date2,date1,  Calendar.HOUR));
		Assert.assertEquals(2880, DateUtils.compareDate(date2,date1,  Calendar.MINUTE));
		Assert.assertEquals(2880*60, DateUtils.compareDate(date2,date1,  Calendar.SECOND));
		
		
		Date date3=DateUtils.string2Date("2018-08-24 20:20:20", DateUtils.DATE_TIME);
		Assert.assertEquals(61, DateUtils.compareDate(date3,date1,  Calendar.DAY_OF_MONTH));
		
		
		
	}

}
