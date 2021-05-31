package com.mawujun.convert.impl;

import com.mawujun.convert.AbstractConverter;
import com.mawujun.util.BooleanUtil;
import com.mawujun.util.StrUtil;

/**
 * 字符转换器
 * 
 * @author mawujun
 *
 */
public class CharacterConverter extends AbstractConverter<Character> {

	@Override
	protected Character convertInternal(Object value) {
		if (char.class == value.getClass()) {
			return Character.valueOf((char) value);
		} else if (value instanceof Boolean) {
			return BooleanUtil.toCharacter((Boolean) value);
		} else if (boolean.class == value.getClass()) {
			return BooleanUtil.toCharacter((boolean) value);
		} else {
			final String valueStr = convertToStr(value);
			if (StrUtil.isNotBlank(valueStr)) {
				return Character.valueOf(valueStr.charAt(0));
			}
		}
		return null;
	}

}
