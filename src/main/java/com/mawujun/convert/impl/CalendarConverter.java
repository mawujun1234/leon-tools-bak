package com.mawujun.convert.impl;

import java.util.Calendar;
import java.util.Date;

import com.mawujun.convert.AbstractConverter;
import com.mawujun.date.DateUtil;
import com.mawujun.util.StrUtil;

/**
 * 日期转换器
 * 
 * @author mawujun
 *
 */
public class CalendarConverter extends AbstractConverter<Calendar> {

	/** 日期格式化 */
	private String format;

	/**
	 * 获取日期格式
	 * 
	 * @return 设置日期格式
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * 设置日期格式
	 * 
	 * @param format 日期格式
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	@Override
	protected Calendar convertInternal(Object value) {
		// Handle Date
		if (value instanceof Date) {
			return DateUtil.calendar((Date)value);
		}

		// Handle Long
		if (value instanceof Long) {
			//此处使用自动拆装箱
			return DateUtil.calendar((Long)value);
		}

		final String valueStr = convertToStr(value);
		return DateUtil.calendar(StrUtil.isBlank(format) ? DateUtil.parse(valueStr) : DateUtil.parse(valueStr, format));
	}

}
