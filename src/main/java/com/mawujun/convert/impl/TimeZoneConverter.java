package com.mawujun.convert.impl;

import java.util.TimeZone;

import com.mawujun.convert.AbstractConverter;

/**
 * TimeZone转换器
 * @author mawujun
 *
 */
public class TimeZoneConverter extends AbstractConverter<TimeZone>{

	@Override
	protected TimeZone convertInternal(Object value) {
		return TimeZone.getTimeZone(convertToStr(value));
	}

}
