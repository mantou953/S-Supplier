package com.xbz.wx;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.common.AppContext;
import com.xbz.http.HttpUtils;
import com.xbz.json.JacksonUtils;

public class WeixinUtils {
	
	private static final Log logger = LogFactory.getLog(WeixinUtils.class);
	
	private static final String CHARSET_UTF8 = "UTF-8";
	
	private static final Map<String, String> ACCESS_TOKEN_MAP = new HashMap<String, String>();

	// 获取OpenId的接口地址
	private static final String OPENID_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
		
	// 获取AccessToken的接口地址
	private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
	
	// 发送模板消息的接口地址
	private static final String SEND_TEMPLATEMSG_URL = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s";
	
	// 获取接口地址
	private static String getHttpURL(String key, String defaultVal, String... values) {
		String httpURL = StringUtils.trim(AppContext.getSystemProperty(key));
		if (StringUtils.isBlank(httpURL)) {
			httpURL = defaultVal;
		}
		return String.format(httpURL, values);
	}
	
	// 获取OpenId
	public static String getOpenId(String appid, String secret, String code) {
		try {
			String httpURL = getHttpURL("wxsupplier.openIdURL", OPENID_URL, appid, secret, code);
			String httpText = HttpUtils.getHtml(httpURL, "UTF-8");
			logger.info("获取OpenId接口的返回结果：" + httpText);
			
			Map returnMap = JacksonUtils.json2Object(httpText, Map.class);
			if (returnMap != null && returnMap.containsKey("openid")) {
				return (String) returnMap.get("openid");
			}
		} catch (Throwable e) {
			logger.error("获取OpenId的过程中出现异常", e);
		}
		return null;
	}
	
	// 获取最新有效的ACCESS_TOKEN
	private static String getNewAccessToken(String appid, String secret) {
		try {
			String httpURL = getHttpURL("wxsupplier.accessTokenURL", ACCESS_TOKEN_URL, appid, secret);
			String httpText = HttpUtils.getHtml(httpURL, CHARSET_UTF8);
			logger.info("获取ACCESS_TOKEN接口的返回结果：" + httpText);
			
			Map returnMap = JacksonUtils.json2Object(httpText, Map.class);
			if (returnMap != null && returnMap.containsKey("access_token")) {
				return (String) returnMap.get("access_token");
			}
		} catch (Throwable e) {
			logger.error("获取ACCESS_TOKEN的过程中出现异常", e);
		}
		return null;
	}
	
	// 刷新ACCESS_TOKEN
	public static synchronized String refreshAccessToken(String appid, String secret) {
		ACCESS_TOKEN_MAP.remove(appid);
		
		String accessToken = getNewAccessToken(appid, secret);
		if (StringUtils.isNotBlank(accessToken)) {
			ACCESS_TOKEN_MAP.put(appid, accessToken);
		}
		
		return accessToken;
	}
	
	// 获取ACCESS_TOKEN
	public static String getAccessToken(String appid, String secret) {
		String accessToken = ACCESS_TOKEN_MAP.get(appid);
		if (StringUtils.isBlank(accessToken)) {
			accessToken = refreshAccessToken(appid, secret);
		}
		return accessToken;
	}
	
	// 发送消息
	public static boolean sendMessage(String appid, String secret, String openId, String templateId, String url, Map<String, Object> data, boolean refreshAccessToken) {
		try {
			String accessToken = null;
			if (refreshAccessToken) {
				accessToken = refreshAccessToken(appid, secret);
			} else {
				accessToken = getAccessToken(appid, secret);
			}
			
			Map<String, Object> httpData = new LinkedHashMap<String, Object>();
			httpData.put("touser", openId);
			httpData.put("template_id", templateId);
			httpData.put("url", url);
			httpData.put("data", data);

			String httpURL = getHttpURL("wxsupplier.sendTemplateMsgURL", SEND_TEMPLATEMSG_URL, accessToken);
			String httpText = HttpUtils.postJson(httpURL, JacksonUtils.object2Json(httpData), CHARSET_UTF8);
			logger.info("发送模板消息接口的返回结果：" + httpText);
			Map returnMap = JacksonUtils.json2Object(httpText, Map.class);
			if(returnMap != null){
				// 如果包含msgid，说明发送成功了
				if(returnMap.containsKey("msgid")){
					return true;
				} else {
					// 如果ACCESS_TOKEN失效了，那么从新发送
					Object errcode = returnMap.get("errcode");
					String errcodeText = (errcode == null ? null : errcode.toString());
					if ("42001".equals(errcodeText) || "41006".equals(errcodeText) || "41003".equals(errcodeText) || "40001".equals(errcodeText)) {
						return sendMessage(appid, secret, openId, templateId, url, data, true);
					}
				}
			}
		} catch (Throwable e) {
			logger.error("获取ACCESS_TOKEN的过程中出现异常", e);
		}
		return false;
	}
	
	// 设置消息变量
	public static void setTemplateItemData(Map<String, Object> data, String key, String value, String color) {
		Map<String, Object> itemData = new HashMap<String, Object>();
		itemData.put("value", value);
		// 消息字体颜色
		if (StringUtils.isNotBlank(color)) {
			itemData.put("color", color);
		}
		data.put(key, itemData);
	}
	
	public static void main(String args[]) {
		String appid = "wxa4fe984bd1827de2";
		String secret = "02227ad70d1612f28cc205c716d9c4e2";
		String openId = "oLI0e1Qwqia-THAtrS4y__TGE738";
		String templateId = "MZlmzGQqj5pBuKRtmYrr-l-t5MAZzSsHaMYTU-uQEfs";
		String url = "http://www.zt-twj.site/seeyon/wxsupplier/bind.html";
		ACCESS_TOKEN_MAP.put(appid, "47_nPNcCo7h4Lk8-6OYNSblBj2XMwyObqUNVnq0K61xJKZfNBkcGEqudQUfxWNAUSmGypDz-xGwFRJxlkLkwP5WkNRIFV3RufOYrLHU4WqY3C9PTxfktctz2nQ3J22Ky-6leAo9glFHA-5fDVKWOZGjAGAZKR");
		
		Map<String, Object> data = new HashMap<String, Object>();
		// {{first.DATA}}
		WeixinUtils.setTemplateItemData(data, "first", "你好，请签名确认", "#173177");
		// {{time.DATA}}
		WeixinUtils.setTemplateItemData(data, "time", "2021-07-22 10:00:00", "#173177");
		// {{ip.DATA}}
		WeixinUtils.setTemplateItemData(data, "ip", "192.168.10.11", "#173177");
		// {{reason.DATA}}
		WeixinUtils.setTemplateItemData(data, "reason", "哈哈哈，测试", "#173177");
		
		boolean flag = WeixinUtils.sendMessage(appid, secret, openId, templateId, url, data, false);
		System.out.println(flag);
		System.out.println(ACCESS_TOKEN_MAP.toString());
	}

}
