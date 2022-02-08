package com.seeyon.apps.wxsupplier.dao.impl;

import java.util.List;
import java.util.Map;

import com.seeyon.apps.wxsupplier.dao.WxSupplierDao;
import com.seeyon.ctp.common.dao.AbstractHibernateDao;
import com.xbz.jdbc.JdbcUtils;
import com.xbz.jdbc.model.SqlContext;

@SuppressWarnings("rawtypes")
public class WxSupplierDaoImpl extends AbstractHibernateDao implements WxSupplierDao {

	public Map<String, Object> queryMap(SqlContext context) {
		try {
			return JdbcUtils.queryMap(getHibernateTemplate().getSessionFactory().getCurrentSession().connection(), context, true);
		} catch (Throwable e) {
			throw new RuntimeException("数据库查询失败", e);
		}
	}

	public List<Map<String, Object>> queryList(SqlContext context) {
		try {
			return JdbcUtils.queryList(getHibernateTemplate().getSessionFactory().getCurrentSession().connection(), context, true);
		} catch (Throwable e) {
			throw new RuntimeException("数据库查询失败", e);
		}
	}

	public int update(SqlContext context) {
		try {
			return JdbcUtils.update(getHibernateTemplate().getSessionFactory().getCurrentSession().connection(), context, true);
		} catch (Throwable e) {
			throw new RuntimeException("数据库更新失败", e);
		}
	}

}
