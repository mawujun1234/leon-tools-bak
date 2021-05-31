package com.mawujun.convert.impl;

import com.mawujun.convert.AbstractConverter;
import com.mawujun.util.ClassUtil;

/**
 * 类转换器<br>
 * 将类名转换为类
 * @author mawujun
 *
 */
public class ClassConverter extends AbstractConverter<Class<?>>{
	
	@Override
	protected Class<?> convertInternal(Object value) {
		String valueStr = convertToStr(value);
		try {
			return ClassUtil.getClassLoader().loadClass(valueStr);
		} catch (Exception e) {
			// Ignore Exception
		}
		return null;
	}

}
