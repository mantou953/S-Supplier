package com.seeyon.apps.wxsupplier.dao.impl;

import java.util.List;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.seeyon.apps.wxsupplier.dao.WxSupplierDao;
import com.xbz.jdbc.model.SqlContext;

public class WxSupplierDao4JdbcTemplateImpl implements WxSupplierDao {

	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public Map<String, Object> queryMap(SqlContext context) {
		try {
			try {
				return jdbcTemplate.queryForMap(context.getSql(), context.getParams());
			} catch (EmptyResultDataAccessException e) {
				return null;
			}
		} catch (Throwable e) {
			throw new RuntimeException("数据库查询失败", e);
		}
	}

	public List<Map<String, Object>> queryList(SqlContext context) {
		try {
			return jdbcTemplate.queryForList(context.getSql(), context.getParams());
		} catch (Throwable e) {
			throw new RuntimeException("数据库查询失败", e);
		}
	}

	public int update(SqlContext context) {
		try {
			return jdbcTemplate.update(context.getSql(), context.getParams());
		} catch (Throwable e) {
			throw new RuntimeException("数据库更新失败", e);
		}
	}

}
