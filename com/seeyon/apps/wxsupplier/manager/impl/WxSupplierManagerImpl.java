package com.seeyon.apps.wxsupplier.manager.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.wxsupplier.dao.WxSupplierDao;
import com.seeyon.apps.wxsupplier.manager.WxSupplierManager;
import com.seeyon.apps.wxsupplier.po.WxSupplier;
import com.seeyon.apps.wxsupplier.util.UploadFileUtil;
import com.seeyon.ctp.util.UUIDLong;
import com.xbz.jdbc.CommonUtils;
import com.xbz.jdbc.JdbcUtils;
import com.xbz.jdbc.TemplateUtils;
import com.xbz.jdbc.model.SqlContext;
import com.xbz.oa.AppUtils;
import com.xbz.wx.WeixinUtils;

public class WxSupplierManagerImpl implements WxSupplierManager {
	
	protected Log logger = LogFactory.getLog(this.getClass());
	
	private WxSupplierDao wxSupplierDao;
	
	public WxSupplierDao getWxSupplierDao() {
		return wxSupplierDao;
	}

	public void setWxSupplierDao(WxSupplierDao wxSupplierDao) {
		this.wxSupplierDao = wxSupplierDao;
	}
	
	// 封装WxSupplier
	private WxSupplier getWxSupplier(String sqlTemplate, Map<String, Object> paramMap) {
		SqlContext context = JdbcUtils.getSqlContext(sqlTemplate, paramMap);

		Map<String, Object> resultMap = wxSupplierDao.queryMap(context);
		if (resultMap == null || resultMap.size() == 0) {
			return null;
		}
		
		WxSupplier supplier = new WxSupplier();
		supplier.setOpenId(AppUtils.getString("openid", resultMap));
		supplier.setSupplierCode(AppUtils.getString("suppliercode", resultMap));
		return supplier;
	}
	
	/**
	 * 根据OpenId获取微信供应商信息
	 * @param openId 微信公众号的OpenId
	 * @return       微信供应商信息
	 */
	public WxSupplier getWxSupplier8OpenId(String openId) {
		if (StringUtils.isBlank(openId)) {
			return null;
		}
		String sqlTemplate = AppUtils.getSystemProperty("wxsupplier.getWxSupplierSql8OpenId");

		Map<String, Object> paramMap = new HashMap<String, Object>(4);
		paramMap.put("openId", openId);
		
		return getWxSupplier(sqlTemplate, paramMap);
	}
	
	/**
	 * 根据供应商编码获取微信供应商信息
	 * @param supplierCode 供应商编码
	 * @return             微信供应商信息
	 */
	public WxSupplier getWxSupplier8SupplierCode(String supplierCode) {
		if (StringUtils.isBlank(supplierCode)) {
			return null;
		}

		String sqlTemplate = AppUtils.getSystemProperty("wxsupplier.getWxSupplier8SupplierCode");

		Map<String, Object> paramMap = new HashMap<String, Object>(4);
		paramMap.put("supplierCode", supplierCode);

		return getWxSupplier(sqlTemplate, paramMap);
	}
	
	/**
	 * 新增微信供应商信息
	 * @param openId       微信公众号的OpenId
	 * @param supplierCode 供应商编码
	 */
	public void insertWxSupplier(String openId, String supplierCode) {
		String sqlTemplate = AppUtils.getSystemProperty("wxsupplier.insertWxSupplier");

		Map<String, Object> paramMap = new HashMap<String, Object>(4);
		paramMap.put("openId", openId);
		paramMap.put("supplierCode", supplierCode);
		
		wxSupplierDao.update(JdbcUtils.getSqlContext(sqlTemplate, paramMap));
	}

	/**
	 * 更新微信供应商信息
	 * @param openId       微信公众号的OpenId
	 * @param supplierCode 供应商编码
	 */
	public void updateWxSupplier(String openId, String supplierCode) {
		String sqlTemplate = AppUtils.getSystemProperty("wxsupplier.updateWxSupplier");

		Map<String, Object> paramMap = new HashMap<String, Object>(4);
		paramMap.put("openId", openId);
		paramMap.put("supplierCode", supplierCode);
		
		wxSupplierDao.update(JdbcUtils.getSqlContext(sqlTemplate, paramMap));
	}

	/**
	 * 删除微信供应商信息
	 * @param openId       微信公众号的OpenId
	 */
	public void deleteWxSupplier(String openId) {
		String sqlTemplate = AppUtils.getSystemProperty("wxsupplier.deleteWxSupplier");

		Map<String, Object> paramMap = new HashMap<String, Object>(4);
		paramMap.put("openId", openId);
		
		wxSupplierDao.update(JdbcUtils.getSqlContext(sqlTemplate, paramMap));
	}
	
	/**
	 * 绑定供应商
	 * @param openId       用户的微信公众的OpenId
	 * @param supplierCode 供应商编码
	 * @param type         操作类型（bind:绑定;unbind:解除绑定;）
	 */
	@Override
	public void bindWxSupplier(String openId, String supplierCode, String type) {
		if (StringUtils.isBlank(openId)) {
			throw new RuntimeException("微信身份标识为空");
		}
		if ("bind".equalsIgnoreCase(type)) {
			if (StringUtils.isBlank(supplierCode)) {
				throw new RuntimeException("供应商编码为空");
			}
			// 根据供应商编号获取绑定关系
			WxSupplier wxSupplier = getWxSupplier8SupplierCode(supplierCode);
			if (wxSupplier != null) {
				if (openId.equals(wxSupplier.getOpenId())) {
					throw new RuntimeException("用户已绑定该供应商，请勿重复操作");
				} else {
					throw new RuntimeException("该供应商已被其他用户绑定");
				}
			}
			
			// 根据OpenId获取绑定关系，如果存在则更新，否则新增
			wxSupplier = getWxSupplier8OpenId(openId);
			if (wxSupplier != null) {
				updateWxSupplier(openId, supplierCode);
			} else {
				insertWxSupplier(openId, supplierCode);
			}
		} else if ("unbind".equalsIgnoreCase(type)) {
			deleteWxSupplier(openId);
		}
	}

	// 更新ctp_attachment表的att_reference、sub_reference、category
	private Long changeCtpAttachment(Long formmainId, Long fileId) {
		// sub_reference
		Long subReference = null;

		// att_reference
		Map<String, Object> paramMap = new HashMap<String, Object>(4);
		paramMap.put("formmainId", formmainId);
		String sqlTemplate = AppUtils.getSystemProperty("wxsupplier.getSummaryIdSql");
		Map<String, Object> dataMap = wxSupplierDao.queryMap(JdbcUtils.getSqlContext(sqlTemplate, paramMap));
		Long attReference = AppUtils.getLong("id", dataMap);

		// 更新ctp_attachment的att_reference、sub_reference、category字段
		if (attReference != null) {
			subReference = UUIDLong.longUUID();

			// 更新参数
			paramMap = new HashMap<String, Object>(8);
			paramMap.put("attReference", attReference);
			paramMap.put("subReference", subReference);
			paramMap.put("fileId", fileId);

			// 更新SQL
			sqlTemplate = AppUtils.getSystemProperty("wxsupplier.updateCtpAttachmentSql");
			wxSupplierDao.update(JdbcUtils.getSqlContext(sqlTemplate, paramMap));
		}

		return subReference;
	}
	
	// 通过summaryId获取
	private Long getFormmainIdBySummaryId(Long summaryId) {
		Long formmainId = null;
		if (summaryId != null) {
			Map<String, Object> paramMap = new HashMap<String, Object>(4);
			paramMap.put("id", summaryId);
			String sqlTemplate = AppUtils.getSystemProperty("wxsupplier.getFormmainId");
			Map<String, Object> dataMap = wxSupplierDao.queryMap(JdbcUtils.getSqlContext(sqlTemplate, paramMap));
			formmainId = AppUtils.getLong("form_recordid", dataMap);
		}
		return formmainId;
	}
		
	/**
	 * 供应商签名
	 * @param id        数据ID
	 * @param signName  供应商签名的Base64字符串
	 * @param tableName 供应商签名的表名称
	 * @param fieldName 供应商签名的字段名称
	 */
	@Override
	public void signName(String id, String signName, String tableName, String fieldName) {
		if (StringUtils.isBlank(id)) {
			throw new RuntimeException("数据ID不能为空");
		}
		if (StringUtils.isBlank(signName)) {
			throw new RuntimeException("签名信息不能为空");
		}
		if (StringUtils.isBlank(tableName)) {
			throw new RuntimeException("数据表信息不能为空");
		}
		
		// 利用接口上传文件
		Long fileId = UploadFileUtil.uploadFile(signName);
		if (fileId == null) {
			throw new RuntimeException("签名文件保存失败");
		}
		
		Long summaryId = CommonUtils.getLong(id);
		
		// 主表ID
		Long formmainId = getFormmainIdBySummaryId(summaryId);
		if (formmainId == null) {
			throw new RuntimeException("获取的主表ID为空");
		}
		
		// 更新ctp_attachment表的att_reference、sub_reference、category，并获取真实文件ID
		Long realFileId = changeCtpAttachment(formmainId, fileId);
		if (realFileId == null) {
			throw new RuntimeException("签名文件保存失败");
		}
		
		// 参数
		Map<String, Object> paramMap = new HashMap<String, Object>(8);
		paramMap.put("id", formmainId);
		paramMap.put("signTime", new Date());
		paramMap.put("signName", signName);
		paramMap.put("fileId", realFileId);

		String sqlTemplate = null;

		// 更新签名信息
		sqlTemplate = AppUtils.getSystemProperty("wxsupplier." + tableName + "UpdateSql");
		wxSupplierDao.update(JdbcUtils.getSqlContext(sqlTemplate, paramMap));

		// 先删除原先签名信息
		sqlTemplate = AppUtils.getSystemProperty("wxsupplier.deleteSignSql");
		wxSupplierDao.update(JdbcUtils.getSqlContext(sqlTemplate, paramMap));

		// 再保存签名信息
		sqlTemplate = AppUtils.getSystemProperty("wxsupplier.insertSignSql");
		wxSupplierDao.update(JdbcUtils.getSqlContext(sqlTemplate, paramMap));
		
		// 待办标题改为已签名
		paramMap.clear();
		paramMap.put("summaryId", summaryId);
		sqlTemplate = AppUtils.getSystemProperty("wxsupplier.updateAffairSql");
		wxSupplierDao.update(JdbcUtils.getSqlContext(sqlTemplate, paramMap));
	}

	/**
	 * 获取对象信息
	 * @param id        数据ID
	 * @param tableName 表名称
	 * @return          对象信息
	 */
	public Map<String, Object> objectInfo(String id, String tableName) {
		// 查询信息
		Map<String, Object> paramMap = new HashMap<String, Object>(4);
		paramMap.put("id", getFormmainIdBySummaryId(CommonUtils.getLong(id)));
		String sqlTemplate = AppUtils.getSystemProperty("wxsupplier." + tableName + "Sql");
		Map<String, Object> dataMap = wxSupplierDao.queryMap(JdbcUtils.getSqlContext(sqlTemplate, paramMap));
		
		// 转换信息
		String sqlField = AppUtils.getSystemProperty("wxsupplier." + tableName + "Field");
		return CommonUtils.change(dataMap, sqlField);
	}

	/**
	 * 获取对象的详情列表信息
	 * @param id        数据ID
	 * @param tableName 表名称
	 * @return          对象的详情列表信息
	 */
	public List<Map<String, Object>> objectList(String id, String tableName) {
		// 查询信息
		Map<String, Object> paramMap = new HashMap<String, Object>(4);
		paramMap.put("id", getFormmainIdBySummaryId(CommonUtils.getLong(id)));
		String sqlTemplate = AppUtils.getSystemProperty("wxsupplier." + tableName + "Sql");
		List<Map<String, Object>> dataMapList = wxSupplierDao.queryList(JdbcUtils.getSqlContext(sqlTemplate, paramMap));
		
		// 转换信息
		String sqlField = AppUtils.getSystemProperty("wxsupplier." + tableName + "Field");
		return CommonUtils.change(dataMapList, sqlField);
	}
	
	// 设置模板消息的属性
	private boolean setData(Map<String, Object> data, String formmainType, String name, Map<String, Object> templateMap) {
		// Key
		String key = AppUtils.getSystemProperty("wxsupplier." + formmainType + name + "Key");
		if (StringUtils.isBlank(key)) {
			return false;
		}

		// Value
		String value = AppUtils.getSystemProperty("wxsupplier." + formmainType + name + "Data");
		if (StringUtils.isNotBlank(value)) {
			value = TemplateUtils.processTemplate(value, templateMap);
		}

		// Color
		String color = AppUtils.getSystemProperty("wxsupplier." + formmainType + name + "Color", "#173177");

		// 设置模板消息的属性
		WeixinUtils.setTemplateItemData(data, key, value, color);

		return true;
	}
	
	/**
	 * 发送消息
	 * @param summaryId    流程ID
	 * @param formmainData 表单数据
	 * @param formmainType 表单类型
	 */
	public void sendMessage(Long summaryId, Map<String, Object> formmainData, String formmainType) {
		logger.info("推送消息: summaryId=" + summaryId + ", formmainType=" + formmainType);
		
		// 厂家
		String changjia = AppUtils.getStringTrim(AppUtils.getSystemProperty("wxsupplier." + formmainType + "Changjia"), formmainData);
		
		// 厂家代码
		String changjiaDaima = AppUtils.getStringTrim(AppUtils.getSystemProperty("wxsupplier." + formmainType + "ChangjiaDaima"), formmainData);;
		
		// 获取OpenId
		String openId = null;
		if (StringUtils.isNotBlank(changjiaDaima)) {
			WxSupplier wxSupplier = getWxSupplier8SupplierCode(changjiaDaima);
			if (wxSupplier != null) {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               				openId = wxSupplier.getOpenId();
			}
		}
		
		logger.info("推送消息: changjia=" + changjia + ", changjiaDaima=" + changjiaDaima + ", summaryId=" + summaryId	+ ", openId=" + openId);
		
		// 消息推送
		if (summaryId != null && StringUtils.isNotBlank(openId)) {
			// 微信公众号的AppId
			String appid = AppUtils.getSystemProperty("wxsupplier.appid");
			// 微信公众号的AppSecret
			String secret = AppUtils.getSystemProperty("wxsupplier.appSecret");
			// 微信公众号的模板消息ID
			String templateId = AppUtils.getSystemProperty("wxsupplier." + formmainType + "MessageTemplateId");
			// 微信公众号的模板消息跳转地址
			String url = String.format(AppUtils.getSystemProperty("wxsupplier." + formmainType + "URL"), summaryId.toString());

			/*
			 * 模板数据
			 */
			Map<String, Object> templateMap = new HashMap<String, Object>();
			// 厂家名称
			templateMap.put("changjia", changjia);
			// 厂家代码
			templateMap.put("changjiaDaima", changjiaDaima);
			// 流程名称
			templateMap.put("billName", AppUtils.getSystemProperty("wxsupplier." + formmainType + "BillName"));
			// 日期时间
			templateMap.put("dateTime", AppUtils.date2String(new Date(), "yyyy-MM-dd HH:mm:ss"));
			// 业务数据
			templateMap.put("formmainData", formmainData);
			
			
			Map<String, Object> data = new HashMap<String, Object>();
			// {{first.DATA}}
			setData(data, formmainType, "First", templateMap);
			// {{remark.DATA}}
			setData(data, formmainType, "Remark", templateMap);
			// {{keywordX.DATA}}
			for (int i = 1; i < 8; i++) {
				if (!setData(data, formmainType, "Keyword" + i, templateMap)) {
					break;
				}
			}
						
			boolean result = WeixinUtils.sendMessage(appid, secret, openId, templateId, url, data, false);
			logger.info("推送消息结果: result=" + result);
		}
	}

	/**
	 * 获取对象的签名信息
	 * @param id        数据ID
	 */
	public String getSignName(String id) {
		// 参数
		Map<String, Object> paramMap = new HashMap<String, Object>(4);
		paramMap.put("id", getFormmainIdBySummaryId(AppUtils.getLong(id)));

		// 查询结果
		String sqlTemplate = AppUtils.getSystemProperty("wxsupplier.getSignSql");
		Map<String, Object> dataMap = wxSupplierDao.queryMap(JdbcUtils.getSqlContext(sqlTemplate, paramMap));

		// 返回签名信息
		String signName = null;
		if (dataMap != null) {
			signName = CommonUtils.getString(dataMap.get("sign_name"));
		}
		return signName;
	}

	/**
	 * 供应商签名验证
	 * @param summaryId    流程ID
	 * @param formmainData 表单数据
	 * @return             已签名返回true，否则返回false
	 */
	public boolean checkSignName(Long summaryId, Map<String, Object> formmainData) {
		logger.info("供应商签名验证: summaryId=" + summaryId);
		return StringUtils.isNotBlank(getSignName(summaryId.toString()));
	}
}
