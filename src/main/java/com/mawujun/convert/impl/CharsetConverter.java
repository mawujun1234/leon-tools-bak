package com.mawujun.convert.impl;

import java.nio.charset.Charset;

import com.mawujun.convert.AbstractConverter;
import com.mawujun.util.CharsetUtil;

/**
 * 编码对象转换器
 * @author Looly
 *
 */
public class CharsetConverter extends AbstractConverter<Charset>{

	@Override
	protected Charset convertInternal(Object value) {
		return CharsetUtil.charset(convertToStr(value));
	}

}
