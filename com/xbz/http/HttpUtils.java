package com.xbz.http;

import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class HttpUtils {
	// 客户端与服务器建立连接的超时时间
	private static final int CONNECT_TIMEOUT = 5000;
	
	// 客户端从服务器读取数据的超时时间
	private static final int READ_TIMEOUT = 50000;
	
	// 从连接池中获取HttpClient的超时时间
	private static final int REQUEST_TIMEOUT = 15000;
	
	private static final RequestConfig REQUEST_CONFIG = RequestConfig.custom().setConnectionRequestTimeout(REQUEST_TIMEOUT).setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(READ_TIMEOUT).build();

	private static CloseableHttpClient httpClient;
	
	private static CloseableHttpClient httpsClient;
	
	private static CloseableHttpClient getCloseableHttpClient(String httpURL) {
		try {
			if (httpURL.startsWith("https:")) {
				if (httpsClient == null) {
					SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustSelfSignedStrategy() {
						public boolean isTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
							return true;
						}
					}).build();
					SSLConnectionSocketFactory ssl = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
					httpsClient = HttpClients.custom().setSSLSocketFactory(ssl).build();
				}
				return httpsClient;
			} else {
				if(httpClient == null){
					httpClient = HttpClients.createDefault();
				}
				return httpClient;
			}
		} catch (Throwable e) {
			return null;
		}
	}
	
	public static String getHtml(String httpURL, String charset) {
		String html = null;
		CloseableHttpResponse response = null;
		try {
			CloseableHttpClient httpClient = getCloseableHttpClient(httpURL);
			HttpGet httpGet = new HttpGet(httpURL);
			httpGet.setConfig(REQUEST_CONFIG);
			response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				html = EntityUtils.toString(entity, charset);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			HttpClientUtils.closeQuietly(response);
		}
		return html;
	}
	
	public static String postJson(String httpURL, String json, String charset) {
		String html = null;
		CloseableHttpResponse response = null;
		try {
			CloseableHttpClient httpClient = getCloseableHttpClient(httpURL);
			HttpPost http = new HttpPost(httpURL);
			http.setConfig(REQUEST_CONFIG);
			http.setEntity(new StringEntity(json, charset));
            response = httpClient.execute(http);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				html = EntityUtils.toString(entity, charset);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			HttpClientUtils.closeQuietly(response);
		}
		return html;
	}
	
	private static List<NameValuePair> createParam(Map<String, Object> paramMap) {
		List<NameValuePair> paramList = new ArrayList<NameValuePair>();
		if (paramMap != null) {
			for (String key : paramMap.keySet()) {
				paramList.add(new BasicNameValuePair(key, paramMap.get(key).toString()));
			}
		}
		return paramList;
	}
	
	public static String post(String httpURL, Map<String, Object> data, String charset) {
		String html = null;
		CloseableHttpResponse response = null;
		try {
			CloseableHttpClient httpClient = getCloseableHttpClient(httpURL);
			HttpPost http = new HttpPost(httpURL);
			http.setConfig(REQUEST_CONFIG);
			http.setEntity(new UrlEncodedFormEntity(createParam(data), charset));
            response = httpClient.execute(http);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				html = EntityUtils.toString(entity, charset);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			HttpClientUtils.closeQuietly(response);
		}
		return html;
	}
	
	public static String postJson4Rest(String httpURL, String json, String charset) {
		String html = null;
		CloseableHttpResponse response = null;
		try {
			// 第一步：创建HttpClient对象
			CloseableHttpClient httpClient = getCloseableHttpClient(httpURL);
			
			// 第二步：创建httpPost对象
			HttpPost http = new HttpPost(httpURL);
			http.setConfig(REQUEST_CONFIG);
			http.setHeader("Content-type", "application/json");
			
			// 第三步：给httpPost设置JSON格式的参数
			StringEntity requestEntity = new StringEntity(json, charset);
			requestEntity.setContentEncoding(charset);
			http.setEntity(requestEntity);
			
			// 第四步：返回结果
			response = httpClient.execute(http);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				html = EntityUtils.toString(entity, charset);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			HttpClientUtils.closeQuietly(response);
		}
		return html;
	}
	
}
