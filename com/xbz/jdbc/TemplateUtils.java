package com.xbz.jdbc;

import java.io.StringWriter;
import java.util.Map;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;

public class TemplateUtils {

	private static final String COMMON_TEMPLATE = "COMMON";

	public static String processTemplate(String content, Map<String, Object> data) {
		try {
			StringTemplateLoader stringLoader = new StringTemplateLoader();
			stringLoader.putTemplate(COMMON_TEMPLATE, content);

			Configuration configuration = new Configuration();
			configuration.setTemplateLoader(stringLoader);

			StringWriter stringWriter = new StringWriter();
			Template template = configuration.getTemplate(COMMON_TEMPLATE, "UTF-8");
			template.process(data, stringWriter);
			return stringWriter.toString();
		} catch (Throwable e) {
			throw new RuntimeException("模板解析失败！");
		}
	}

}
