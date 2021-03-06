package com.mawujun.convert.impl;

import java.util.Locale;

import com.mawujun.convert.AbstractConverter;
import com.mawujun.util.StrUtil;

/**
 * 
 * {@link Locale}对象转换器<br>
 * 只提供String转换支持
 * 
 * @author mawujun
 * @since 4.5.2
 */
public class LocaleConverter extends AbstractConverter<Locale> {

	@Override
	protected Locale convertInternal(Object value) {
		try {
			String str = convertToStr(value);
			if (StrUtil.isEmpty(str)) {
				return null;
			}

			final String[] items = str.split("_");
			if (items.length == 1) {
				return new Locale(items[0]);
			}
			if (items.length == 2) {
				return new Locale(items[0], items[1]);
			}
			return new Locale(items[0], items[1], items[2]);
		} catch (Exception e) {
			// Ignore Exception
		}
		return null;
	}

}
