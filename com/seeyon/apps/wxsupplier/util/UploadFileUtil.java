package com.seeyon.apps.wxsupplier.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.util.Base64;
import com.xbz.http.HttpUtils;
import com.xbz.json.JacksonUtils;
import com.xbz.oa.AppUtils;

public class UploadFileUtil {
	
    private static final Log logger = LogFactory.getLog(UploadFileUtil.class);
    
    private static final String REST_USERNAME = "wbrest";
    
    private static final String REST_PASSWORD = "d64a8190-6425-4398-a46e-08fd79d843fa";
    
    private static final String REST_LOGINNAME = "test0";
    
    private static final String REST_TOKENURL = "http://39.106.35.129:8080/seeyon/rest/token";
    
    private static final String REST_UPLOADURL = "http://39.106.35.129:8080/seeyon/rest/attachment?token=%s&applicationCategory=0&extensions=&firstSave=true";
    
    private static final ConcurrentHashMap<String, Token> TOKEN_CACHE = new ConcurrentHashMap<String, Token>();
    
    public static class Token {

    	// 产生的时间
    	private Date activeTime;

    	// Token信息
    	private String accessToken;

    	public Token(Date activeTime, String accessToken) {
    		this.activeTime = activeTime;
    		this.accessToken = accessToken;
    	}

    	public Date getActiveTime() {
    		return activeTime;
    	}

    	public void setActiveTime(Date activeTime) {
    		this.activeTime = activeTime;
    	}

    	public String getAccessToken() {
    		return accessToken;
    	}

    	public void setAccessToken(String accessToken) {
    		this.accessToken = accessToken;
    	}

    	/**
    	 * 验证token是否有效，失效返回true，生效返回false
    	 */
    	public boolean isInvalid(long activeMinutes) {
    		long currentTimeMillis = System.currentTimeMillis();
    		long activeTimeMillis = activeTime.getTime();
    		return currentTimeMillis - activeTimeMillis > activeMinutes * 60000L;
    	}

    }
    
	// 获取配置属性
	private static String getProperty(String key, String defaultVal) {
		String value = StringUtils.trim(AppContext.getSystemProperty(key));
		if (StringUtils.isBlank(value)) {
			value = defaultVal;
		}
		return value;
	}
    
    // 获取Token
	private static synchronized String getNewToken() {
		try {
			String restTokenURL = getProperty("wxsupplier.restTokenURL", REST_TOKENURL);

			String restUserName = getProperty("wxsupplier.restUserName", REST_USERNAME);

			String restPassword = getProperty("wxsupplier.restPassword", REST_PASSWORD);
			
			String restLoginName = getProperty("wxsupplier.restLoginName", REST_LOGINNAME);

			Map<String, Object> data = new HashMap<String, Object>();
			data.put("userName", restUserName);
			data.put("password", restPassword);
			data.put("loginName", restLoginName);
			String httpText = HttpUtils.postJson4Rest(restTokenURL, JacksonUtils.object2Json(data), "UTF-8");
			Map returnMap = JacksonUtils.json2Object(httpText, Map.class);
			if (returnMap != null && returnMap.containsKey("id")) {
				return (String) returnMap.get("id");
			}
		} catch (Throwable e) {
			logger.error("获取Token的过程中出现异常", e);
		}
		return null;
	}
	
	// 获取Token
	public static String getToken() {
		// 缓存Key
		String cacheKey = getProperty("wxsupplier.restUserName", REST_USERNAME);

		// 从缓存中获取Token信息
		Token token = TOKEN_CACHE.get(cacheKey);

		// 如果缓存中不存在或者Token已失效
		if (token == null || token.isInvalid(12)) {
			token = new Token(new Date(), getNewToken());
			TOKEN_CACHE.put(cacheKey, token);
		}
		return token.getAccessToken();
	}
	
	// 获取签名图片名称
	private static String getSignImgName() {
		return "sign_" + UUID.randomUUID().toString().replaceAll("-", "").toLowerCase() + ".png";
	}
	
	// 获取文件ID
	private static Long findFileId(String jsonText) {
		Map returnMap = JacksonUtils.json2Object(jsonText, Map.class);
		if (returnMap == null) {
			return null;
		}
		List atts = (List) returnMap.get("atts");
		if (atts == null || atts.size() == 0) {
			return null;
		}
		return AppUtils.getLong("id", (Map) atts.get(0));
	}

	// 上传文件
    public static Long uploadFile(String fileName, InputStream inputStream) {
        Long id = null;
        try {
        	String token = getToken();
            URL url = new URL(String.format(getProperty("wxsupplier.restUploadURL", REST_UPLOADURL), token));
            // 设置请求头
            HttpURLConnection hc = (HttpURLConnection) url.openConnection();
            hc.setDoOutput(true);
            hc.setUseCaches(false);
            hc.setRequestProperty("contentType", "charset=utf-8");
            hc.setRequestMethod("POST");
            hc.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + "-----");
            
            DataOutputStream dos = new DataOutputStream(hc.getOutputStream());
            StringBuffer sb = new StringBuffer();
            sb.append("--");
            sb.append("-----");
            sb.append("\r\n");
            sb.append("Content-Disposition: form-data; \r\n name=\"1\"; filename=\"" + fileName + "\"\r\n");
            sb.append("Content-Type: image/png\r\n\r\n");
            dos.write(sb.toString().getBytes("UTF-8"));
            BufferedInputStream input = new BufferedInputStream(inputStream);
            int data = 0;
            while ((data = input.read()) != -1) {
                dos.write(data);
            }
            dos.write(("\r\n--" + "-----" + "--\r\n").getBytes());
            dos.flush();
            dos.close();
            StringBuffer result = new StringBuffer();
            InputStream is = hc.getInputStream();
            int ch;
            while ((ch = is.read()) != -1) {
                result.append((char) ch);
            }
            if (is != null) {
                is.close();
            }
            if (input != null) {
                input.close();
            }

			String json = result.toString();
			if(StringUtils.isNotBlank(json)){
				id = findFileId(json);
				logger.info("附件上传成功: id=" + id);
			} else {
				logger.info("附件上传失败");
			}
        } catch (Throwable e) {
            logger.error("附件上传失败", e);
        }
        return id;
    }
    
	// 上传BASE64文件g
	public static Long uploadFile(String base64Img) {
		InputStream inputStream = null;
		try {
			String fileName = getSignImgName();
			if (base64Img.startsWith("data:image/png;base64,")) {
				base64Img = base64Img.substring(22);
			}
			inputStream = new ByteArrayInputStream(Base64.decodeBase64(base64Img.getBytes("UTF-8")));
			return uploadFile(fileName, inputStream);
		} catch (Throwable e) {

		} finally{
			IOUtils.closeQuietly(inputStream);
		}
		return null;
	}

    public static void main(String args[]) throws Exception {
    	String base64Img = IOUtils.toString(new java.io.FileInputStream("E:/22.txt"));
    	Long id = UploadFileUtil.uploadFile(base64Img);
    	System.out.println(id);
    }
}
