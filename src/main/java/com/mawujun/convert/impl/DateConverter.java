package com.mawujun.convert.impl;

import java.util.Calendar;

import com.mawujun.convert.AbstractConverter;
import com.mawujun.date.DateTime;
import com.mawujun.date.DateUtil;
import com.mawujun.util.StrUtil;

/**
 * 日期转换器
 * 
 * @author mawujun
 *
 */
public class DateConverter extends AbstractConverter<java.util.Date> {

	private Class<? extends java.util.Date> targetType;
	/** 日期格式化 */
	private String format;

	/**
	 * 构造
	 * 
	 * @param targetType 目标类型
	 */
	public DateConverter(Class<? extends java.util.Date> targetType) {
		this.targetType = targetType;
	}

	/**
	 * 构造
	 * 
	 * @param targetType 目标类型
	 * @param format 日期格式
	 */
	public DateConverter(Class<? extends java.util.Date> targetType, String format) {
		this.targetType = targetType;
		this.format = format;
	}

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
	protected java.util.Date convertInternal(Object value) {
		Long mills = null;
		if (value instanceof Calendar) {
			// Handle Calendar
			mills = ((Calendar) value).getTimeInMillis();
		} else if (value instanceof Long) {
			// Handle Long
			mills = (Long) value;
		} else {
			// 统一按照字符串处理
			final String valueStr = convertToStr(value);
			try {
				mills = StrUtil.isBlank(this.format) ? DateUtil.parse(valueStr).getTime() : DateUtil.parse(valueStr, this.format).getTime();
			} catch (Exception e) {
				// Ignore Exception
			}
		}

		if (null == mills) {
			return null;
		}

		// 返回指定类型
		if (java.util.Date.class == targetType) {
			return new java.util.Date(mills);
		}
		if (DateTime.class == targetType) {
			return new DateTime(mills);
		} else if (java.sql.Date.class == targetType) {
			return new java.sql.Date(mills);
		} else if (java.sql.Time.class == targetType) {
			return new java.sql.Time(mills);
		} else if (java.sql.Timestamp.class == targetType) {
			return new java.sql.Timestamp(mills);
		}

		throw new UnsupportedOperationException(StrUtil.format("Unsupport Date type: {}", this.targetType.getName()));
	}

}
