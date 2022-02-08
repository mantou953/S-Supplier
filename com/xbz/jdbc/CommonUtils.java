package com.xbz.jdbc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.xbz.jdbc.model.FieldFix;

import java.sql.Clob;
import java.text.SimpleDateFormat;

public class CommonUtils {
	
	private static final Map<String, Integer> TYPE_CASE_MAP = new HashMap<String, Integer>();
	
	static {
		TYPE_CASE_MAP.put("byte", 0);
		TYPE_CASE_MAP.put("short", 1);
		TYPE_CASE_MAP.put("integer", 2);
		TYPE_CASE_MAP.put("long", 3);
		TYPE_CASE_MAP.put("float", 4);
		TYPE_CASE_MAP.put("double", 5);
		TYPE_CASE_MAP.put("boolean", 6);
		TYPE_CASE_MAP.put("string", 7);
		TYPE_CASE_MAP.put("decimal", 8);
		TYPE_CASE_MAP.put("string2date", 9);
		TYPE_CASE_MAP.put("date2string", 10);
	}
	
	public static Byte getByte(Object value) {
		if (value == null || value instanceof Byte) {
			return (Byte) value;
		}
		return Byte.parseByte(value.toString());
	}

	public static Short getShort(Object value) {
		if (value == null || value instanceof Short) {
			return (Short) value;
		}
		return Short.parseShort(value.toString());
	}

	public static Integer getInteger(Object value) {
		if (value == null || value instanceof Integer) {
			return (Integer) value;
		}
		return Integer.parseInt(value.toString());
	}

	public static Long getLong(Object value) {
		if (value == null || value instanceof Long) {
			return (Long) value;
		}
		return Long.parseLong(value.toString());
	}

	public static Float getFloat(Object value) {
		if (value == null || value instanceof Float) {
			return (Float) value;
		}
		return Float.parseFloat(value.toString());
	}

	public static Double getDouble(Object value) {
		if (value == null || value instanceof Double) {
			return (Double) value;
		}
		return Double.parseDouble(value.toString());
	}

	public static Boolean getBoolean(Object value) {
		if (value == null || value instanceof Boolean) {
			return (Boolean) value;
		}
		String string = value.toString();
		if ("true".equalsIgnoreCase(string) || "1".equals(string)) {
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	}

	public static String getString(Object value) {
		if ((value instanceof Clob)) {
			Clob clob = (Clob) value;
			try {
				return clob.getSubString(1L, (int) clob.length());
			} catch (Exception e) {
				return null;
			}
		}
		return value == null ? null : value.toString();
	}

	public static BigDecimal getBigDecimal(Object value) {
		if (value == null || value instanceof BigDecimal) {
			return (BigDecimal) value;
		}
		return new BigDecimal(value.toString());
	}

	public static Date string2Date(Object value, String format) {
		if (value == null || value instanceof Date) {
			return (Date) value;
		}
		try {
			String date = value.toString();
			return new SimpleDateFormat(format).parse(date);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static String date2String(Object value, String format) {
		if (value instanceof Date) {
			return new SimpleDateFormat(format).format((Date) value);
		}
		if (value == null) {
			return null;
		}
		return value.toString();
	}
	
	private static Map<String, FieldFix> getFieldFixMap(String fieldFixString) {
		Map<String, FieldFix> fieldFixMap = new HashMap<String, FieldFix>();
		if (StringUtils.isNotBlank(fieldFixString)) {
			String[] fieldFixArray = StringUtils.split(fieldFixString, ';');
			for (String fieldFix : fieldFixArray) {
				String[] fields = StringUtils.split(fieldFix, ',');
				if (fields == null || fields.length < 2) {
					continue;
				}
				FieldFix ff = new FieldFix();
				ff.setSrcField(fields[0]);
				ff.setNewField(fields[1]);
				if (fields.length > 2) {
					ff.setNewType(fields[2]);
				}
				if (fields.length > 3) {
					ff.setFormat(fields[3]);
				}
				fieldFixMap.put(ff.getSrcField(), ff);
			}
		}
		return fieldFixMap;
	}

	// 转换Map的属性
	public static Map<String, Object> change(Map<String, Object> dataMap, Map<String, FieldFix> fieldFixMap) {
		if (fieldFixMap == null || fieldFixMap.size() == 0 || dataMap == null || dataMap.size() == 0) {
			return dataMap;
		}
		Map<String, Object> dataMapNew = new HashMap<String, Object>();

		Iterator<Entry<String, Object>> entries = dataMap.entrySet().iterator();
		while (entries.hasNext()) {
			Entry<String, Object> entry = entries.next();
			String key = entry.getKey();
			Object val = entry.getValue();
			FieldFix fieldFix = fieldFixMap.get(key);
			if (fieldFix != null) {
				key = fieldFix.getNewField();
				if (StringUtils.isNotBlank(fieldFix.getNewType())) {
					Integer type = TYPE_CASE_MAP.get(fieldFix.getNewType().toLowerCase());
					if (type == null) {
						type = Integer.valueOf(-1);
					}
					switch (type) {
					case 0:
						val = getByte(val);
						break;
					case 1:
						val = getShort(val);
						break;
					case 2:
						val = getInteger(val);
						break;
					case 3:
						val = getLong(val);
						break;
					case 4:
						val = getFloat(val);
						break;
					case 5:
						val = getDouble(val);
						break;
					case 6:
						val = getBoolean(val);
						break;
					case 7:
						val = getString(val);
						break;
					case 8:
						val = getBigDecimal(val);
						break;
					case 9:
						val = string2Date(val, fieldFix.getFormat());
						break;
					case 10:
						val = date2String(val, fieldFix.getFormat());
						break;
					default:
						break;
					}
				}
			}
			dataMapNew.put(key, val);
		}
		return dataMapNew;
	}

	// 转换Map的属性
	public static Map<String, Object> change(Map<String, Object> dataMap, String fieldFixString) {
		return change(dataMap, getFieldFixMap(fieldFixString));
	}

	// 转换List的属性
	public static List<Map<String, Object>> change(List<Map<String, Object>> list, Map<String, FieldFix> fieldFixMap) {
		if (list == null || list.size() == 0) {
			return list;
		}
		List<Map<String, Object>> listNew = new ArrayList<Map<String, Object>>(list.size());
		for (Map<String, Object> dataMap : list) {
			listNew.add(change(dataMap, fieldFixMap));
		}
		return listNew;
	}

	// 转换List的属性
	public static List<Map<String, Object>> change(List<Map<String, Object>> list, String fieldFixString) {
		return change(list, getFieldFixMap(fieldFixString));
	}

}
