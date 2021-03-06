package com.xbz.jdbc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.xbz.jdbc.model.SqlContext;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

public class JdbcUtils {
	
	private static final String PARAM_REGEX = "\\{\\s*[a-zA-Z0-9_.]+\\s*\\}";

	private static final Map<Class<?>, Integer> TYPE_MAP = new HashMap<Class<?>, Integer>();

	static {
		TYPE_MAP.put(String.class, Types.VARCHAR);
		TYPE_MAP.put(Character.class, Types.VARCHAR);

		TYPE_MAP.put(Byte.class, Types.BIT);
		TYPE_MAP.put(Short.class, Types.SMALLINT);
		TYPE_MAP.put(Integer.class, Types.INTEGER);
		TYPE_MAP.put(Long.class, Types.BIGINT);
		TYPE_MAP.put(Float.class, Types.FLOAT);
		TYPE_MAP.put(Double.class, Types.DOUBLE);
		TYPE_MAP.put(Boolean.class, Types.BOOLEAN);

		TYPE_MAP.put(java.util.Date.class, Types.TIMESTAMP);
		TYPE_MAP.put(java.sql.Date.class, Types.DATE);
		TYPE_MAP.put(java.sql.Timestamp.class, Types.TIMESTAMP);
		TYPE_MAP.put(java.sql.Time.class, Types.TIME);

	}

	private static String getPreparedSql(String sql) {
		return sql == null ? null : sql.replaceAll(PARAM_REGEX, "?");
	}

	private static List<String> findParams(String sql) {
		List<String> paramList = new ArrayList<String>();
		if (sql != null) {
			Matcher matcher = Pattern.compile(PARAM_REGEX).matcher(sql);
			while (matcher.find()) {
				paramList.add(matcher.group());
			}
		}
		return paramList;
	}

	private static String getParamName(String param) {
		if (param != null && param.length() > 2) {
			return param.substring(1, param.length() - 1).trim();
		}
		return "";
	}

	private static int getJdbcType(Object param) {
		if (param == null) {
			return Types.JAVA_OBJECT;
		}
		Integer jdbcType = TYPE_MAP.get(param.getClass());
		return jdbcType == null ? Types.JAVA_OBJECT : jdbcType.intValue();
	}

	private static SqlContext createSqlContext(String sql, Map<String, Object> paramMap) {
		SqlContext sqlContext = new SqlContext();
		// SQL
		sqlContext.setSql(getPreparedSql(sql));
		// ??????
		List<String> paramList = findParams(sql);
		Object[] params = new Object[paramList.size()];
		for (int i = 0; i < paramList.size(); i++) {
			params[i] = paramMap.get(getParamName(paramList.get(i)));
		}
		sqlContext.setParams(params);
		// ????????????
		if ((params != null) && (params.length > 0)) {
			int[] paramTypes = new int[params.length];
			for (int i = 0; i < params.length; i++) {
				paramTypes[i] = getJdbcType(params[i]);
			}
			sqlContext.setParamTypes(paramTypes);
		}
		return sqlContext;
	}

	/**
	 * ???????????????SQL?????????
	 * @param sqlTemplate SQL??????
	 * @param paramMap    SQL??????
	 * @return            SQL?????????
	 */
	public static SqlContext getSqlContext(String sqlTemplate, Map<String, Object> paramMap) {
		return createSqlContext(TemplateUtils.processTemplate(sqlTemplate, paramMap), paramMap);
	}
	
	// ????????????
	private static void setParameter(PreparedStatement preparedStatement, Object[] params, int[] paramTypes) throws SQLException {
		for (int i = 0, len = params.length; i < len; i++) {
			preparedStatement.setObject(i + 1, params[i], paramTypes[i]);
		}
	}
	
	// ????????????
	private static Map<String, Object> getResultMap(ResultSet resultSet, ResultSetMetaData metaData) throws SQLException {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		for (int i = 1, len = metaData.getColumnCount(); i <= len; i++) {
			resultMap.put(metaData.getColumnLabel(i).toLowerCase(), resultSet.getObject(i));
		}
		return resultMap;
	}
	
	/**
	 * ????????????????????????
	 */
	public static Map<String, Object> queryMap(Connection connection, SqlContext context, boolean closeConnection) throws SQLException {
		Map<String, Object> resultMap = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			// ???????????????
			preparedStatement = connection.prepareStatement(context.getSql());
			// ????????????
			setParameter(preparedStatement, context.getParams(), context.getParamTypes());
			// ????????????
			resultSet = preparedStatement.executeQuery();
			// ????????????
			if (resultSet.next()) {
				resultMap = getResultMap(resultSet, resultSet.getMetaData());
			}
			return resultMap;
		} finally {
			close(resultSet);
			close(preparedStatement);
			if (closeConnection) {
				close(connection);
			}
		}
	}
	
	/**
	 * ????????????????????????
	 */
	public static List<Map<String, Object>> queryList(Connection connection, SqlContext context, boolean closeConnection) throws SQLException {
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			// ???????????????
			preparedStatement = connection.prepareStatement(context.getSql());
			// ????????????
			setParameter(preparedStatement, context.getParams(), context.getParamTypes());
			// ????????????
			resultSet = preparedStatement.executeQuery();
			// ????????????
			while (resultSet.next()) {
				resultList.add(getResultMap(resultSet, resultSet.getMetaData()));
			}
			return resultList;
		} finally {
			close(resultSet);
			close(preparedStatement);
			if (closeConnection) {
				close(connection);
			}
		}
	}
	
	/**
	 * ???????????????
	 */
	public static int update(Connection connection, SqlContext context, boolean closeConnection) throws SQLException {
		PreparedStatement preparedStatement = null;
		try {
			// ???????????????
			preparedStatement = connection.prepareStatement(context.getSql());
			// ????????????
			setParameter(preparedStatement, context.getParams(), context.getParamTypes());
			// ??????????????????
			return preparedStatement.executeUpdate();
		} finally {
			close(preparedStatement);
			if (closeConnection) {
				close(connection);
			}
		}
	}
	
	// ????????????
	public static void commit(Connection connection) throws SQLException {
		if (connection != null) {
			connection.commit();
		}
	}

	// ????????????
	public static void rollback(Connection connection) throws SQLException {
		if (connection != null) {
			connection.rollback();
		}
	}

	// ????????????
	public static void close(Connection connection) throws SQLException {
		if (connection != null) {
			connection.close();
		}
	}

	// ??????Statement
	public static void close(Statement statement) throws SQLException {
		if (statement != null) {
			statement.close();
		}
	}

	// ??????PreparedStatement
	public static void close(PreparedStatement preparedStatement) throws SQLException {
		if (preparedStatement != null) {
			preparedStatement.close();
		}
	}

	// ??????CallableStatement
	public static void close(CallableStatement callableStatement) throws SQLException {
		if (callableStatement != null) {
			callableStatement.close();
		}
	}

	// ??????ResultSet
	public static void close(ResultSet resultSet) throws SQLException {
		if (resultSet != null) {
			resultSet.close();
		}
	}

}
