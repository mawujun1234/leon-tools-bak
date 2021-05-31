package com.mawujun.convert.impl;

import com.mawujun.convert.AbstractConverter;
import com.mawujun.util.BooleanUtil;

/**
 * 波尔转换器
 * @author mawujun
 *
 */
public class BooleanConverter extends AbstractConverter<Boolean>{

	@Override
	protected Boolean convertInternal(Object value) {
		if(boolean.class == value.getClass()){
			return Boolean.valueOf((boolean)value);
		}
		String valueStr = convertToStr(value);
		return Boolean.valueOf(BooleanUtil.toBoolean(valueStr));
	}

}
