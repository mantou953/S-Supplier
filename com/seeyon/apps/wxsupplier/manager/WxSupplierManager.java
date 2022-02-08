package com.seeyon.apps.wxsupplier.manager;

import java.util.List;
import java.util.Map;

import com.seeyon.apps.wxsupplier.po.WxSupplier;

public interface WxSupplierManager {
	
	/**
	 * 根据OpenId获取微信供应商信息
	 * @param openId 微信公众号的OpenId
	 * @return       微信供应商信息
	 */
	WxSupplier getWxSupplier8OpenId(String openId);
	
	/**
	 * 根据供应商编码获取微信供应商信息
	 * @param supplierCode 供应商编码
	 * @return             微信供应商信息
	 */
	WxSupplier getWxSupplier8SupplierCode(String supplierCode);
	
	/**
	 * 新增微信供应商信息
	 * @param openId       微信公众号的OpenId
	 * @param supplierCode 供应商编码
	 */
	void insertWxSupplier(String openId, String supplierCode);
	
	/**
	 * 更新微信供应商信息
	 * @param openId       微信公众号的OpenId
	 * @param supplierCode 供应商编码
	 */
	void updateWxSupplier(String openId, String supplierCode);
	
	/**
	 * 删除微信供应商信息
	 * @param openId       微信公众号的OpenId
	 */
	void deleteWxSupplier(String openId);
	
	/**
	 * 绑定供应商
	 * @param openId       用户的微信公众的OpenId
	 * @param supplierCode 供应商编码
	 * @param type         操作类型（bind:绑定;unbind:解除绑定;）
	 */
	void bindWxSupplier(String openId, String supplierCode, String type);
	
	/**
	 * 供应商签名
	 * @param id        数据ID
	 * @param signName  供应商签名的Base64字符串
	 * @param tableName 供应商签名的表名称
	 * @param fieldName 供应商签名的字段名称
	 */
	void signName(String id, String signName, String tableName, String fieldName);
	
	/**
	 * 获取对象信息
	 * @param id        数据ID
	 * @param tableName 表名称
	 * @return          对象信息
	 */
	Map<String, Object> objectInfo(String id, String tableName);
	
	/**
	 * 获取对象的详情列表信息
	 * @param id        数据ID
	 * @param tableName 表名称
	 * @return          对象的详情列表信息
	 */
	List<Map<String, Object>> objectList(String id, String tableName);
	
	/**
	 * 发送消息
	 * @param summaryId    流程ID
	 * @param formmainData 表单数据
	 * @param formmainType 表单类型
	 */
	void sendMessage(Long summaryId, Map<String, Object> formmainData, String formmainType);
	
	/**
	 * 获取对象的签名信息
	 * @param id        数据ID
	 */
	String getSignName(String id);
	
	/**
	 * 供应商签名验证
	 * @param summaryId    流程ID
	 * @param formmainData 表单数据
	 * @return             已签名返回true，否则返回false
	 */
	boolean checkSignName(Long summaryId, Map<String, Object> formmainData);
}
