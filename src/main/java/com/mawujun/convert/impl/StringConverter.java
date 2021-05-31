package com.mawujun.convert.impl;

import com.mawujun.convert.AbstractConverter;

/**
 * 字符串转换器
 * @author mawujun
 *
 */
public class StringConverter extends AbstractConverter<String>{

	@Override
	protected String convertInternal(Object value) {
		return convertToStr(value);
	}

}
