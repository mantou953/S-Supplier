package com.xbz.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class JacksonUtils {
	
	public static class JacksonContainerClass<T> {
		private final Class<T> containerClass;
		
		private final Class<?>[] genericsClasses;

		public JacksonContainerClass(Class<T> containerClass, Class<?>[] genericsClasses) {
			this.containerClass = containerClass;
			this.genericsClasses = genericsClasses;
		}

		public Class<T> getContainerClass() {
			return this.containerClass;
		}

		public Class<?>[] getGenericsClasses() {
			return this.genericsClasses;
		}

		public boolean isSimple() {
			return this.containerClass != null && this.genericsClasses == null;
		}
	}
	
	// 日期自定义类
	private static class CustomDateDeSerializer extends DateDeserializers.DateDeserializer {

		private static final long serialVersionUID = 4777663341080070589L;

		public Date deserialize(JsonParser jsonParser, DeserializationContext dc) throws IOException, JsonProcessingException {
			String text = (jsonParser == null ? null : jsonParser.getText());
			if (text == null || text.length() == 0) {
				return null;
			}
				
			Date date = null;
			
			// 父类方法
			try {
				date = super.deserialize(jsonParser, dc);
			} catch (Throwable e) {
				// ignore
			}
			if (date != null) {
				return date;
			}
			
			// 自定义方法
			try {
				DateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy h:m:s aa", Locale.ENGLISH);
				return dateFormat.parse(text);
			} catch (Throwable e) {
				
			}
			return null;
		}
	}

	private static ObjectMapper objectMapper = new ObjectMapper();
	
	static {
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		/*
		 * 属性为null时不进行序列化
		 * objectMapper.setSerializationInclusion(Include.NON_EMPTY);
		 */

		// 日期处理类
		SimpleModule deserializeMoudle = new SimpleModule();
		deserializeMoudle.addDeserializer(Date.class, new CustomDateDeSerializer());
		objectMapper.registerModule(deserializeMoudle);
	}

	public static final ObjectMapper getObjectMapper() {
		if (objectMapper == null) {
			objectMapper = new ObjectMapper();
		}
		return objectMapper;
	}

	@SuppressWarnings("unchecked")
	public static final <T> T json2Object(String json, TypeReference<T> type) {
		try {
			return  type == null ? null : (T)getObjectMapper().readValue(json, type);
		} catch (Exception e) {
			throw new RuntimeException("Json string deserialization failed", e);
		} 
	}

	public static final <T> T json2Object(String json, Class<T> type) {
		try {
			return type == null ? null : getObjectMapper().readValue(json, type);
		} catch (Exception e) {
			throw new RuntimeException("Json string deserialization failed", e);
		} 
	}

	private static JavaType getJavaType(Class<?> containerClass, Class<?>... genericsClasses) {
		return getObjectMapper().getTypeFactory().constructParametricType(containerClass, genericsClasses);
	}

	public static final <T> T json2Object(String json, Class<?> containerClass, Class<?>... genericsClasses) {
		JavaType javaType = getJavaType(containerClass, genericsClasses);
		try {
			return getObjectMapper().readValue(json, javaType);
		} catch (Exception e) {
			throw new RuntimeException("Json string deserialization failed", e);
		} 
	}

	public static final <T> T json2Object(String json, JacksonContainerClass<T> entityClass) {
		if (entityClass.isSimple()) {
			return json2Object(json, entityClass.getContainerClass());
		} else {
			return json2Object(json, entityClass.getContainerClass(), entityClass.getGenericsClasses());
		}
	}

	public static final String object2Json(Object object) {
		try {
			return getObjectMapper().writeValueAsString(object);
		} catch (Exception e) {
			throw new RuntimeException("Object serialization failed", e);
		}
	}

}
