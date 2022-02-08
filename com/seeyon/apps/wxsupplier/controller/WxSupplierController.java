package com.seeyon.apps.wxsupplier.controller;

import com.seeyon.apps.wxsupplier.manager.WxSupplierManager;
import com.seeyon.apps.wxsupplier.po.WxSupplier;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.util.annotation.NeedlessCheckLogin;
import com.xbz.json.JacksonUtils;
import com.xbz.oa.AppUtils;
import com.xbz.web.WebUtils;
import com.xbz.web.vo.ResponseResult;
import com.xbz.wx.WeixinUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WxSupplierController extends BaseController {

	private static final Log logger = LogFactory.getLog(WxSupplierController.class);

	private WxSupplierManager wxSupplierManager;

	public WxSupplierManager getWxSupplierManager() {
		return wxSupplierManager;
	}

	public void setWxSupplierManager(WxSupplierManager wxSupplierManager) {
		this.wxSupplierManager = wxSupplierManager;
	}
	
	/**
	 * 获取供应商信息
	 */
	@NeedlessCheckLogin
	public ModelAndView getWxSupplier(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String code = request.getParameter("code");
		String appid = AppUtils.getSystemProperty("wxsupplier.appid");
		String secret = AppUtils.getSystemProperty("wxsupplier.appSecret");
		logger.info("微信公众号网页授权: code=" + code);
		
		// 用户的微信公众的OpenId
		String openId = WeixinUtils.getOpenId(appid, secret, code);
		logger.info("获取供应商信息: openId=" + openId);

		ResponseResult<WxSupplier> result = null;
		try {
			WxSupplier wxSupplier = wxSupplierManager.getWxSupplier8OpenId(openId);
			if (wxSupplier == null) {
				wxSupplier = new WxSupplier();
				wxSupplier.setOpenId(openId);
			}
			result = ResponseResult.success(wxSupplier);
		} catch (RuntimeException e) {
			logger.error("获取供应商信息的过程中出现异常", e);
			result = ResponseResult.fail(e.getMessage());
		} catch (Throwable e) {
			logger.error("获取供应商信息的过程中出现异常", e);
			result = ResponseResult.fail("系统错误");
		}
		WebUtils.sendJSON(request, response, JacksonUtils.object2Json(result), "UTF-8");
		return null;
	}
	
	/**
	 * 绑定供应商
	 */
	@NeedlessCheckLogin
	public ModelAndView bindWxSupplier(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// 用户的微信公众的OpenId
		String openId = request.getParameter("openId");

		// 供应商编码
		String supplierCode = request.getParameter("supplierCode");

		// 操作类型
		String type = request.getParameter("type");
		
		logger.info("绑定供应商: openId=" + openId + ", supplierCode=" + supplierCode + ", type=" + type);

		ResponseResult<Object> result = null;
		try {
			wxSupplierManager.bindWxSupplier(openId, supplierCode, type);
			result = ResponseResult.success();
		} catch (RuntimeException e) {
			logger.error("绑定供应商的过程中出现异常", e);
			result = ResponseResult.fail(e.getMessage());
		} catch (Throwable e) {
			logger.error("绑定供应商的过程中出现异常", e);
			result = ResponseResult.fail("系统错误");
		}
		WebUtils.sendJSON(request, response, JacksonUtils.object2Json(result), "UTF-8");
		return null;
	}
	
	/**
	 * 供应商签名
	 */
	@NeedlessCheckLogin
	public ModelAndView signName(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// 数据ID
		String id = request.getParameter("id");

		// 供应商签名的Base64字符串
		String signName = request.getParameter("signName");

		// 供应商签名的表名称
		String tableName = request.getParameter("tableName");
		
		// 供应商签名的字段名称
		String fieldName = request.getParameter("fieldName");
		
		logger.info("供应商签名: id=" + id + ", tableName=" + tableName + ", fieldName=" + fieldName);

		ResponseResult<Object> result = null;
		try {
			wxSupplierManager.signName(id, signName, tableName, fieldName);
			result = ResponseResult.success("签名成功");
		} catch (RuntimeException e) {
			logger.error("保存供应商签名的过程中出现异常", e);
			result = ResponseResult.fail(e.getMessage());
		} catch (Throwable e) {
			logger.error("保存供应商签名的过程中出现异常", e);
			result = ResponseResult.fail("系统错误");
		}
		WebUtils.sendJSON(request, response, JacksonUtils.object2Json(result), "UTF-8");
		return null;
	}
	
	/**
	 * 获取对象信息
	 */
	@NeedlessCheckLogin
	public ModelAndView objectInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// 数据ID
		String id = request.getParameter("id");

		// 表名称
		String tableName = request.getParameter("tableName");

		logger.info("获取对象信息: id=" + id + ", tableName=" + tableName);

		ResponseResult<Object> result = null;
		try {
			Map<String, Object> data = wxSupplierManager.objectInfo(id, tableName);
			
			// 获取签名信息
			if (data != null) {
				String signName = wxSupplierManager.getSignName(id);
				data.put("signName", signName);
			}
			result = ResponseResult.success((Object) data);
		} catch (RuntimeException e) {
			logger.error("获取对象信息的过程中出现异常", e);
			result = ResponseResult.fail(e.getMessage());
		} catch (Throwable e) {
			logger.error("获取对象信息的过程中出现异常", e);
			result = ResponseResult.fail("系统错误");
		}
		WebUtils.sendJSON(request, response, JacksonUtils.object2Json(result), "UTF-8");
		return null;
	}

	/**
	 * 获取对象的详情列表信息
	 */
	@NeedlessCheckLogin
	public ModelAndView objectList(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// 数据ID
		String id = request.getParameter("id");

		// 表名称
		String tableName = request.getParameter("tableName");

		logger.info("获取对象的详情列表信息: id=" + id + ", tableName=" + tableName);

		ResponseResult<Object> result = null;
		try {
			result = ResponseResult.success((Object) wxSupplierManager.objectList(id, tableName));
		} catch (RuntimeException e) {
			logger.error("获取对象的详情列表信息的过程中出现异常", e);
			result = ResponseResult.fail(e.getMessage());
		} catch (Throwable e) {
			logger.error("获取对象的详情列表信息的过程中出现异常", e);
			result = ResponseResult.fail("系统错误");
		}
		WebUtils.sendJSON(request, response, JacksonUtils.object2Json(result), "UTF-8");
		return null;
	}
}
