package com.mawujun.validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mawujun.regex.RegexUtil;

public class ValidUtil {
	/**
	 * 验证一个号码是不是手机号
	 * 
	 * @param mobiles
	 * @return
	 */
	public static boolean isMobile(String mobile) {
//		if(mobiles==null || "".equals(mobiles.trim())) {
//			return false;
//		}
//
//		Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0-9])|(14[5,7])| (17[0,1,3,5-8]))\\d{8}$");
//		Matcher m = p.matcher(mobiles);
//		return m.matches();
		return RegexUtil.checkMobile(mobile);
	}
	/**
	 * 验证一个号码是不是电话号码
	 * @param phoneNumber
	 * @return
	 */
	public static boolean isTel(String telNumber) {
//		boolean isValid = false;
//		// String
//		// expression="((^(\\d{2,4}[-_－—]?)?\\d{3,8}([-_－—]+\\d{3,8})?([-_－—]+\\d{1,7})?$)|(^0?1[35]\\d{9}$))";
//		String expression = "((^((0\\d{2,3})-)(\\d{7,8})(-(\\d{3,}))?$))";
//		CharSequence inputStr = telNumber;
//		Pattern pattern = Pattern.compile(expression);
//		Matcher matcher = pattern.matcher(inputStr);
//		if (matcher.matches()) {
//			isValid = true;
//		}
//		return isValid;
		
		return RegexUtil.checkPhone(telNumber);
	}
	/**
	 * 判断电子邮件是否正确
	 * @param value
	 * @return
	 */
	public static boolean isEmail(String email) {
//        String emailPattern = "^([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)*@([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)+[\\.][A-Za-z]{2,3}([\\.][A-Za-z]{2})?$";
//        Pattern p = Pattern.compile(emailPattern);
//        Matcher m = p.matcher(value);
//        return m.matches();
		return RegexUtil.checkEmail(email);
    }
}
