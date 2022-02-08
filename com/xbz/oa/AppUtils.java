package com.xbz.oa;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.seeyon.ctp.common.AppContext;

public class AppUtils {

	// 获取系统配置属性
	public static String getSystemProperty(String key) {
		return StringUtils.trim(AppContext.getSystemProperty(key));
	}

	// 获取系统配置属性
	public static String getSystemProperty(String key, String defaultVal) {
		String value = getSystemProperty(key);
		return StringUtils.isBlank(value) ? defaultVal : value;
	}

	// 获取字符串
	public static String getString(Object value) {
		return value == null ? null : value.toString();
	}
	
	// 获取Key的值
	public static Object getValue(String key, Map<?, ?> dataMap) {
		return dataMap == null ? null : dataMap.get(key);
	}

	// 从Map中获取字符串
	public static String getString(String key, Map<?, ?> dataMap) {
		return getString(getValue(key, dataMap));
	}

	// 从Map中获取字符串
	public static String getStringTrim(String key, Map<?, ?> dataMap) {
		return StringUtils.trim(getString(key, dataMap));
	}
	
	// 获取Long值
	public static Long getLong(Object value) {
		if (value == null || value instanceof Long) {
			return (Long) value;
		}
		return Long.parseLong(value.toString());
	}
	
	// 从Map中获取Long值
	public static Long getLong(String key, Map<?, ?> dataMap) {
		return getLong(getValue(key, dataMap));
	}
	
	// 日期格式化
	public static String date2String(Date date, String format) {
		return new SimpleDateFormat(format).format(date);
	}
}
