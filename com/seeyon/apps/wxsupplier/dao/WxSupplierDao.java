package com.seeyon.apps.wxsupplier.dao;

import java.util.List;
import java.util.Map;

import com.xbz.jdbc.model.SqlContext;

public interface WxSupplierDao {

	Map<String, Object> queryMap(SqlContext context);

	List<Map<String, Object>> queryList(SqlContext context);

	int update(SqlContext context);
}
