package com.seeyon.apps.wxsupplier.po;

import java.io.Serializable;

/**
 * 微信供应商
 */
public class WxSupplier implements Serializable {

	private static final long serialVersionUID = -1803833329066752040L;

	// 微信公众号的OpenId
	private String openId;

	// 微信供应商的唯一编号
	private String supplierCode;

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	public String getSupplierCode() {
		return supplierCode;
	}

	public void setSupplierCode(String supplierCode) {
		this.supplierCode = supplierCode;
	}

}
