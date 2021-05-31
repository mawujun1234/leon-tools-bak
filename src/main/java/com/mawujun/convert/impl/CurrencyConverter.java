package com.mawujun.convert.impl;

import java.util.Currency;

import com.mawujun.convert.AbstractConverter;

/**
 * 货币{@link Currency} 转换器
 * 
 * @author mawujun
 * @since 3.0.8
 */
public class CurrencyConverter extends AbstractConverter<Currency> {

	@Override
	protected Currency convertInternal(Object value) {
		return Currency.getInstance(convertToStr(value));
	}

}
