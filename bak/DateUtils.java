package com.mawujun.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mawujun.exception.BizException;

public class DateUtils extends org.apache.commons.lang3.time.DateUtils{

    
    private static Logger logger = LoggerFactory.getLogger(DateUtils.class);
	/**
	 * yyyy-MM-dd
	 */
	public static final String DATE_SHORT = "yyyy-MM-dd";
	
	public static final String DATE_SHORT1 = "yyyyMMdd";
	/**
	 * yyyy-MM-dd HH:mm:ss
	 */
	public static final String DATE_TIME="yyyy-MM-dd HH:mm:ss";
	
	
	private static final String date_pattern_file="date.pattern.properties";
	private static final String date_pattern_prefix="regular";
	
	static HashMap<String,SimpleDateFormat> cache_format=new HashMap<String,SimpleDateFormat>();
	static {

		cache_format.put(DATE_SHORT1, new SimpleDateFormat("yyyyMMdd"));
		cache_format.put(DATE_SHORT, new SimpleDateFormat("yyyy-MM-dd"));
		cache_format.put(DATE_TIME, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
	}
	
	private static Map<String, String> regularFormat_map=new LinkedHashMap<String,String>(){{
		
		this.put("^\\d{4}-\\d{1,2}-\\d{1,2}$", "yyyy-MM-dd");
		this.put("^\\d{4}-\\d{1,2}-\\d{1,2} {1}\\d{1,2}:\\d{1,2}:\\d{1,2}$", "yyyy-MM-dd HH:mm:ss");
		this.put("^\\d{4}-\\d{1,2}$", "yyyy-MM");
		this.put("^\\d{4}$", "yyyy");
		this.put("^\\d{4}-\\d{1,2}-\\d{1,2} {1}\\d{1,2}$","yyyy-MM-dd HH" );
		this.put("^\\d{4}-\\d{1,2}-\\d{1,2} {1}\\d{1,2}:\\d{1,2}$","yyyy-MM-dd HH:mm" );	
		this.put("^\\d{1,2}:\\d{1,2}:\\d{1,2}$", "HH:mm:ss");
	}};
	
	static {
		Properties properties=PropertiesUtils.load(date_pattern_file).getProperties();
		if(properties!=null || properties.size()>0) {
			for(Entry<Object,Object> entry:properties.entrySet()) {
    			String key=entry.getKey().toString();
    			if(key.indexOf(date_pattern_prefix)==0) {
    				DateUtils.addDatePatterns(key.substring(key.indexOf('.')+1), (String)entry.getValue());
    			}
			}
		}
	}
	public static void addDatePatterns(String java_pattern,String regular) {
		regularFormat_map.put(regular, java_pattern);
	}
	/**
	 * 获取日期相关的格式化模板
	 * @return
	 */
	public static String[] getDatePatterns() {
		String[] patterns=new String[regularFormat_map.size()];
		int i=0;
		for(Entry<String,String> entry:regularFormat_map.entrySet()) {
			patterns[i]=entry.getValue();
			i++;
		}
		return patterns;
	}
	/**
	 * 只解析24小时制的
	 * 解析日期字符串的格式
	 * 现在能解析的格式还不全
	 * @param date_sr
	 * @return
	 */
	public static String resolverDateFormat(String date_sr) {
		String value = date_sr.trim();
        if ("".equals(value)) {
            return null;
        }
        String format=null;
        for(Entry<String,String> entry:regularFormat_map.entrySet()) {
        	if(value.matches(entry.getKey())) {
        		format= entry.getValue();
        		break;
        	}
        	
        }
        if(format==null) {
        	 throw new IllegalArgumentException("暂时不支持这个格式的解析 '" + date_sr + "',需要新增的话，新建date.pattern.properties文件，按regular.yyyy-MM-dd=^\\\\d{4}-\\\\d{1,2}-\\\\d{1,2}$这种模式编写");
        }
        return format;
//        if(value.matches("^\\d{4}-\\d{1,2}$")){
//        	return "yyyy-MM";
//            //return parseDate(source, formarts.get(0));
//        }else if(value.matches("^\\d{4}-\\d{1,2}-\\d{1,2}$")){
//        	return "yyyy-MM-dd";
//            //return parseDate(source, formarts.get(1));
//        }else if(value.matches("^\\d{4}-\\d{1,2}-\\d{1,2} {1}\\d{1,2}:\\d{1,2}$")){
//        	return "yyyy-MM-dd HH:mm";
//           // return parseDate(source, formarts.get(2));
//        }else if(value.matches("^\\d{4}-\\d{1,2}-\\d{1,2} {1}\\d{1,2}:\\d{1,2}:\\d{1,2}$")){
//        	return "yyyy-MM-dd HH:mm:ss";
//            //return parseDate(source, formarts.get(3));
//        }else {
//            throw new IllegalArgumentException("暂时不支持这个格式的解析 '" + date_sr + "'");
//        }

	}

	public static Date long2Date(long var0) {
		Calendar var2 = Calendar.getInstance();
		var2.setTimeInMillis(var0);
		return var2.getTime();
	}

	public static String long2String(long var0) {
		return date2String(long2Date(var0), DATE_TIME);
	}

	public static long date2Long(Date var0) {
		Calendar var1 = Calendar.getInstance();
		var1.setTime(var0);
		return var1.getTimeInMillis();
	}

//	/**
//	 * 
//	 * @param date
//	 * @param format DateUtils.DATE_TIME
//	 * @return
//	 */
//	public static String date2String(Date date, String format) {
//		return date == null ? " " : getSimpleDateFormat(format).format(date);
//	}
	
	public static SimpleDateFormat getSimpleDateFormat(String format) {
		SimpleDateFormat var2 = cache_format.get(format);
		if(format==null) {
			var2=new SimpleDateFormat(format);
			cache_format.put(format,var2);
		}
		return var2;
	}

	public static String date2String(Date date, String format) {
		SimpleDateFormat var2 = getSimpleDateFormat(format);
		
		return var2.format(date);
	}


	/**
	 * 输出指定的日期时间格式 yyy-MM-dd HH:mm:ss
	 * @param var0
	 * @return
	 */
	public static String date2String(Date var0) {
		SimpleDateFormat format = getSimpleDateFormat(DATE_TIME);
		return format.format(var0);
	}

    /**
     * 字符串日期格式变成long类型
     * @param datestr
     * @param format
     * @return
     */
	public static long string2Long(String datestr, String format) {
		return date2Long(string2Date(datestr, format));
	}

	public static Date getDateNow() {
		Calendar var0 = Calendar.getInstance();
		return var0.getTime();
	}
	/**
	 * 获取当前的日期时间  yyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static String getStringNow() {
		return date2String(getDateNow(), DATE_TIME);
	}
	/**
	 * 返回的格式为yyy-MM-dd
	 * @return
	 */
	public static String getStringNowYmd() {
		Calendar var1 = Calendar.getInstance();
		SimpleDateFormat format = getSimpleDateFormat(DATE_SHORT);
		return format.format(var1.getTime());
	}
	/**
	 * 返回的格式为yyyMMdd
	 * @return
	 */
	public static String getStringNowYmd1() {
		Calendar var1 = Calendar.getInstance();
		SimpleDateFormat format = getSimpleDateFormat(DATE_SHORT1);
		return format.format(var1.getTime());
	}

	/**
	 * 自定义日期格式
	 * @param DateUtils.DATE_TIME
	 * @return
	 */
	public static String getStringNow(String formatstr) {
		Calendar var1 = Calendar.getInstance();
		SimpleDateFormat format = getSimpleDateFormat(formatstr);
		return format.format(var1.getTime());
	}

	public static long getLongNow() {
		return date2Long(getDateNow());
	}
	/**
	 * 把指定格式的日期转换为日期类型
	 * @param var0
	 * @param var1
	 * @return
	 */
	public static Date string2Date(String datestr, String formatstr) {
		SimpleDateFormat format = getSimpleDateFormat(formatstr);
		try {
			return format.parse(datestr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new BizException("日期格式化失败",e);
		}
//		if (var0==null || "".equals(var0)) {
//			return Calendar.getInstance().getTime();
//		} else {
//			if (var1 == -1) {
//				int var2 = var0.indexOf("-");
//				if (var2 > 0) {
//					var2 = var0.indexOf(":");
//					if (var2 > 0) {
//						var1 = 1;
//					} else if (var0.indexOf("-", var2 + 1) > 0) {
//						var1 = 0;
//					} else {
//						var1 = 8;
//					}
//				} else {
//					var2 = var0.indexOf(":");
//					if (var2 <= 0) {
//						return Calendar.getInstance().getTime();
//					}
//
//					var1 = 2;
//				}
//			}
//
//			DateFormat var8 = cache_format.get(var1);
//			Date var3 = null;
//
//			String var5;
//            try {
//                var3 = var8.parse(var0);
//            } catch (ParseException var6) {
//                var5 = "解析日期{" + var0 + "}格式{" + ((SimpleDateFormat)var8).toPattern() + "}异常!";
//                if (logger.isDebugEnabled()) {
//                	logger.error(var5, var6);
//                } else {
//                	logger.error(var5);
//                }
//            } catch (Exception var7) {
//                var3 = Calendar.getInstance().getTime();
//                var5 = "转换日期{" + var0 + "}为{" + ((SimpleDateFormat)var8).toPattern() + "}是格式时异常!";
//                if (logger.isDebugEnabled()) {
//                	logger.error(var5, var7);
//                } else {
//                	logger.error(var5);
//                }
//            }
//
//			return var3;
//		}
	}
	/**
	 * 把当前日期和参数日期进行比较，当前时间减去参数的时间
	 * 第二个参数是Calendar.SECOND,Calendar.MINUTE,Calendar.HOUR,Calendar.DAY_OF_MONTH
	 * @param var0
	 * @param var1
	 * @return
	 */
	public static int compareDate(Date date, int var1) {
		return compareDate(date, Calendar.getInstance().getTime(), var1);
	}

	/**
	 * 
	 * @param date
	 * @param field Calendar.SECOND,Calendar.MINUTE,Calendar.HOUR,Calendar.DAY_OF_MONTH
	 * @param var2
	 * @return
	 */
	public static Date addDate(Date date, int field, int var2) {
		if (field < 0) {
			logger.warn("日期增减操作时不支持类型:" + field);
			return date;
		} else {
			Calendar var3 = Calendar.getInstance();
			var3.setTime(date);
			var3.add(field, var2);
			return var3.getTime();
		}
	}

	public static Date addTodayDate(int var0, int var1) {
		return addDate(getDateNow(), var0, var1);
	}

	/**
	 * 比较两个日期相差天数，last-pre
	 * @param var0
	 * @param var1
	 * @param var2
	 * @return
	 */
	public static int compareDay(Date pre, Date last) {
		return compareDate(pre, last,Calendar.DAY_OF_MONTH);
	}
	/**
	 *  比较两个日期的差距，后面一个日期减去前面的一个日期:last-pre
	 * @param pre
	 * @param last
	 * @param var2 Calendar.SECOND,Calendar.MINUTE,Calendar.HOUR,Calendar.DAY_OF_MONTH
	 * @return
	 */
	public static int compareDate(Date pre, Date last, int var2) {
		long var3 = last.getTime() - pre.getTime();
		if (var2 == Calendar.MINUTE) {
			var3 = var3 / 60000;
		} else if (var2 == Calendar.HOUR) {
			var3 = var3 / 3600000;
		} else if (var2 == Calendar.DAY_OF_MONTH) {
			var3 = var3 / 86400000;
		} else if (var2 == Calendar.SECOND) {
			var3 = var3 / 1000;
		}

		return (int) var3;
		
//		double var3 = (double) (var1.getTime() - var0.getTime());
//		if (var2 == Calendar.MINUTE) {
//			var3 = fixDouble2(var3 / 60000.0D);
//		} else if (var2 == Calendar.HOUR) {
//			var3 = fixDouble2(var3 / 3600000.0D);
//		} else if (var2 == Calendar.DAY_OF_MONTH) {
//			var3 = fixDouble2(var3 / 8.64E7D);
//		} else if (var2 == Calendar.SECOND) {
//			var3 = var3 / 1000;
//		}
//
//		return (int) var3;
	}
//	private static DecimalFormat decimalFormat =new DecimalFormat("#.00");
//	public static double fixDouble2(double var0) {
//		return fixDouble(var0, 2);
//	}
//
//	public static double fixDouble(double var0, int var2) {
//		if (var2 < 0) {
//			var2 = 2;
//		}
//
//		DecimalFormat var3 = null;
//		if (var2 == 2) {
//			var3 = decimalFormat;
//		} else {
//			String var4 = String.valueOf((int) Math.pow(10.0D, (double) var2));
//			var3 = new DecimalFormat(var2 == 0 ? "#" : "#." + var4.substring(1));
//		}
//
//		return Double.parseDouble(var3.format(var0));
//	}
	
}
