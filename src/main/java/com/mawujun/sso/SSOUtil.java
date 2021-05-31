package com.mawujun.sso;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 通过java进行后台转发的模式进行单点登录
 */
public class SSOUtil {
	//使用的demo
//	//@POST
//	@Path("/ssocloud/{var:.*}")
//	public void sso() throws IOException {
//		String method = request.getMethod();
//		String url = request.getRequestURI();
//		String contentType = request.getContentType();
//		int base = request.getContextPath().length();
//		System.out.println(request.getSession().getId());
//		String host = request.getContextPath()+url.substring(base + offset, url.length());//例如: trade
//		logger.info("request.getRequestURI():"+request.getRequestURI()+"\tbase:"+base+"\thost:"+host);
//		url=RedirectUtil.getHost("ssocloud", host);
//		if("GET".equals(method)){
//			HttpUtil.httpGet(url,request, response, new ArrayList<NameValuePair>());
//		}else if("POST".equals(method)){
//			if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
////                MultipartHttpServletRequest multipartRequest =
////                        WebUtils.getNativeRequest(request, MultipartHttpServletRequest.class);
////                MultipartFile file = multipartRequest.getFile("file");
////                HttpUtil.uploadFile(url,file,"file",request);
//			}else{
//				HttpUtil.httpPost(url,request, response, new ArrayList<NameValuePair>());
//			}
//		}else if("PUT".equals(method)){
//			HttpUtil.httpPut(url,request, response, new ArrayList<NameValuePair>());
//		}else if("DELETE".equals(method)) {
//			HttpUtil.httpDelete(url,request, response, new ArrayList<NameValuePair>());
//		}
//		response.getOutputStream().flush();
//		response.getOutputStream().close();
//
//	}

	private static Logger log=LoggerFactory.getLogger(SSOUtil.class);

	public static void login(String url, HttpServletRequest request,List<NameValuePair> additionalMap) {
		CloseableHttpClient httpClient = HttpClients.createDefault();//SSLUtils.createSSLClientDefault();
		HttpPost httpPost = new HttpPost(url);

		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			if("Content-Length".equalsIgnoreCase(headerName)) {
				continue;
			}
			httpPost.addHeader(headerName, request.getHeader(headerName));
		}
		addCookieHeader(request,httpPost);

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		Enumeration<String> parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String parameterName = parameterNames.nextElement();
			for (String value : request.getParameterValues(parameterName)) {
				if (value != null && !value.equals("null"))
					params.add(new BasicNameValuePair(parameterName, value));
			}
		}
		params.addAll(additionalMap);

		for (NameValuePair map : additionalMap) {
			log.info(map.getName() + ":" + map.getValue());
		}
		HttpResponse httpResponse = null;
		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, RedirectUtil.SERVER_CHARSET);
			httpPost.setEntity(entity);
			httpResponse = httpClient.execute(httpPost);
			if (httpResponse != null) {
				HttpEntity responseEntity = httpResponse.getEntity();
				if (responseEntity != null) {
					log.info(responseEntity.toString());
					//responseEntity.writeTo(response.getOutputStream());
				}
			}
		} catch (IOException e) {
			log.error("发送post请求失败:",e);
			//e.printStackTrace();
		}
		if (httpResponse != null) {
//			response.setStatus(httpResponse.getStatusLine().getStatusCode());
////logger.info(httpResponse.toString());
			HeaderIterator headerIterator = httpResponse.headerIterator();
			while (headerIterator.hasNext()) {
				Header header = headerIterator.nextHeader();
//				if (header.getName().equals("Content-Type")) {
////response.addHeader(header.getName(), header.getValue());
//					response.setHeader(header.getName(), header.getValue());// 或许可以解决重定向乱码(好像没影响)
//				}
				//缓存登录的cookie
				if("Set-Cookie".equalsIgnoreCase(header.getName())) {
					cookies.put(request.getSession().getId(), header.getValue());
				}
			}
			//response.setHeader("Server", "nginx");
		}
	}
	
	public static void httpGet(String url, HttpServletRequest request, HttpServletResponse response,
			List<NameValuePair> additionalMap) {
		CloseableHttpClient httpClient = HttpClients.createDefault();//SSLUtils.createSSLClientDefault();

		URIBuilder uriBuilder =null;
		try {
			uriBuilder = new URIBuilder(url);
			//把url后面的地址复制过来并按原样传递过去
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			Enumeration<String> parameterNames = request.getParameterNames();
			while (parameterNames.hasMoreElements()) {
				String parameterName = parameterNames.nextElement();
				for (String value : request.getParameterValues(parameterName)) {
					if (value != null && !value.equals("null"))
						params.add(new BasicNameValuePair(parameterName, value));
				}
			}
			params.addAll(additionalMap);

			uriBuilder.setParameters(params);
			url=uriBuilder.build().toString();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpGet httpGet = new HttpGet(url);

		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			if("Host".equalsIgnoreCase(headerName) || "Referer".equalsIgnoreCase(headerName)) {
				continue;
			}
			httpGet.addHeader(headerName, request.getHeader(headerName));
		}
		
		addCookieHeader(request,httpGet);

		HttpResponse httpResponse = null;
		try {
//UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, RedirectUtil.SERVER_CHARSET);
//httpGet.setEntity(entity);
			httpResponse = httpClient.execute(httpGet);
			if (httpResponse != null) {
				HttpEntity responseEntity = httpResponse.getEntity();
				if (responseEntity != null) {
					log.info(responseEntity.toString());
					responseEntity.writeTo(response.getOutputStream());
				}
			}
		} catch (IOException e) {
			log.error("发送get请求失败:",e);
			//e.printStackTrace();
		}
		if (httpResponse != null) {
			response.setStatus(httpResponse.getStatusLine().getStatusCode());
//logger.info(httpResponse.toString());
			HeaderIterator headerIterator = httpResponse.headerIterator();
			while (headerIterator.hasNext()) {
				Header header = headerIterator.nextHeader();
				if (header.getName().equals("Content-Type")) {
//response.addHeader(header.getName(), header.getValue());
					response.setHeader(header.getName(), header.getValue());// 或许可以解决重定向乱码(好像没影响)
				}
			}
			response.setHeader("Server", "nginx");
		}
	}

	public static void httpPost(String url, HttpServletRequest request, HttpServletResponse response,
			List<NameValuePair> additionalMap) {
		CloseableHttpClient httpClient = HttpClients.createDefault();//SSLUtils.createSSLClientDefault();
		HttpPost httpPost = new HttpPost(url);

		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			if("Content-Length".equalsIgnoreCase(headerName)) {
				continue;
			}
			httpPost.addHeader(headerName, request.getHeader(headerName));
		}

		addCookieHeader(request,httpPost);

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		Enumeration<String> parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String parameterName = parameterNames.nextElement();
			for (String value : request.getParameterValues(parameterName)) {
				if (value != null && !value.equals("null"))
					params.add(new BasicNameValuePair(parameterName, value));
			}
		}
		params.addAll(additionalMap);

		for (NameValuePair map : additionalMap) {
			log.info(map.getName() + ":" + map.getValue());
		}
		HttpResponse httpResponse = null;
		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, RedirectUtil.SERVER_CHARSET);
			httpPost.setEntity(entity);
			httpResponse = httpClient.execute(httpPost);
			if (httpResponse != null) {
				HttpEntity responseEntity = httpResponse.getEntity();
				if (responseEntity != null) {
					log.info(responseEntity.toString());
					responseEntity.writeTo(response.getOutputStream());
				}
			}
		} catch (IOException e) {
			log.error("发送post请求失败:",e);
			//e.printStackTrace();
		}
		if (httpResponse != null) {
			response.setStatus(httpResponse.getStatusLine().getStatusCode());
//logger.info(httpResponse.toString());
			HeaderIterator headerIterator = httpResponse.headerIterator();
			while (headerIterator.hasNext()) {
				Header header = headerIterator.nextHeader();
				if (header.getName().equals("Content-Type")) {
//response.addHeader(header.getName(), header.getValue());
					response.setHeader(header.getName(), header.getValue());// 或许可以解决重定向乱码(好像没影响)		
				}
				//缓存登录时的cookie
				if("Set-Cookie".equalsIgnoreCase(header.getName())) {
					cookies.put(request.getSession().getId(), header.getValue());
				}
			}
			//response.setHeader("Server", "nginx");
		}
	}
	//这里会内存泄漏，注意修改
	static Map<String,String> cookies=new HashMap<String,String>();

	/**
	 * 把登陆时缓存的cookie，添加到后续的请求中，这样就可以保证请求不跳到认证界面
	 * @param request
	 * @param http
	 */
	private static  void addCookieHeader(HttpServletRequest request,HttpRequestBase http) {
		//以当前系统的会话为id，以另一个系统的cookie为值进行缓存
		String value=cookies.get(request.getSession().getId());
		if(value!=null) {
			//http.addHeader("Set-Cookie",value);
			http.addHeader("Cookie",value);
		}
	}


	public static void httpPut(String url, HttpServletRequest request, HttpServletResponse response,
			List<NameValuePair> additionalMap) {
		CloseableHttpClient httpClient = HttpClients.createDefault();//SSLUtils.createSSLClientDefault();

		HttpPut httpPut = new HttpPut(url);

		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			httpPut.addHeader(headerName, request.getHeader(headerName));
		}
		addCookieHeader(request,httpPut);

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		Enumeration<String> parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String parameterName = parameterNames.nextElement();
			for (String value : request.getParameterValues(parameterName)) {
				if (value != null && !value.equals("null"))
					params.add(new BasicNameValuePair(parameterName, value));
			}
		}
		params.addAll(additionalMap);
		for (NameValuePair map : additionalMap) {
			log.info(map.getName() + ":" + map.getValue());
		}
		HttpResponse httpResponse = null;
		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, RedirectUtil.SERVER_CHARSET);
			httpPut.setEntity(entity);
			httpResponse = httpClient.execute(httpPut);
			if (httpResponse != null) {
				HttpEntity responseEntity = httpResponse.getEntity();
				if (responseEntity != null) {
					log.info(responseEntity.toString());
					responseEntity.writeTo(response.getOutputStream());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (httpResponse != null) {
			response.setStatus(httpResponse.getStatusLine().getStatusCode());
//logger.info(httpResponse.toString());
			HeaderIterator headerIterator = httpResponse.headerIterator();
			while (headerIterator.hasNext()) {
				Header header = headerIterator.nextHeader();
				if (header.getName().equals("Content-Type")) {
//response.addHeader(header.getName(), header.getValue());
					response.setHeader(header.getName(), header.getValue());// 或许可以解决重定向乱码(好像没影响)
				}
			}
			//response.setHeader("Server", "nginx");
		}
	}

	public static void httpDelete(String url, HttpServletRequest request, HttpServletResponse response,
			List<NameValuePair> additionalMap) {
		String method = request.getMethod();
		CloseableHttpClient httpClient = HttpClients.createDefault();//SSLUtils.createSSLClientDefault();

		HttpDelete httpDelete = new HttpDelete(url);

		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			httpDelete.addHeader(headerName, request.getHeader(headerName));
		}
		addCookieHeader(request,httpDelete);

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		Enumeration<String> parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String parameterName = parameterNames.nextElement();
			for (String value : request.getParameterValues(parameterName)) {
				if (value != null && !value.equals("null"))
					params.add(new BasicNameValuePair(parameterName, value));
			}
		}
		params.addAll(additionalMap);
		for (NameValuePair map : additionalMap) {
			log.info(map.getName() + ":" + map.getValue());
		}
		HttpResponse httpResponse = null;
		try {
//UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, RedirectUtil.SERVER_CHARSET);
//httpDelete.setEntity(entity);
			httpResponse = httpClient.execute(httpDelete);
			if (httpResponse != null) {
				HttpEntity responseEntity = httpResponse.getEntity();
				if (responseEntity != null) {
					log.info(responseEntity.toString());
					responseEntity.writeTo(response.getOutputStream());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (httpResponse != null) {
			response.setStatus(httpResponse.getStatusLine().getStatusCode());
//logger.info(httpResponse.toString());
			HeaderIterator headerIterator = httpResponse.headerIterator();
			while (headerIterator.hasNext()) {
				Header header = headerIterator.nextHeader();
				if (header.getName().equals("Content-Type")) {
//response.addHeader(header.getName(), header.getValue());
					response.setHeader(header.getName(), header.getValue());// 或许可以解决重定向乱码(好像没影响)
				}
			}
			//response.setHeader("Server", "nginx");
		}
	}

	public static String httpRequest(String hostPrefix, String url, List<NameValuePair> params) {
		CloseableHttpClient httpClient = HttpClients.createDefault();//SSLUtils.createSSLClientDefault();
		log.info(url);
		HttpPost httpPost = new HttpPost(RedirectUtil.getHost(hostPrefix, url));
		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, RedirectUtil.SERVER_CHARSET);
			httpPost.setEntity(entity);
			HttpResponse httpResponse = httpClient.execute(httpPost);
			if (httpResponse != null) {
				HttpEntity responseEntity = httpResponse.getEntity();
				if (responseEntity != null) {
					String result = EntityUtils.toString(responseEntity, RedirectUtil.SERVER_CHARSET);
					log.info(result);
					return result;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

//	/**
//	 * 使用httpclint 发送文件
//	 * 
//	 * @author: qingfeng
//	 * @date: 2019-05-27
//	 * @param file 上传的文件
//	 * @return 响应结果
//	 */
//	public static void uploadFile(String url, MultipartFile file, String fileParamName, HttpServletRequest request) {
//		try {
//			RestTemplateUtils restTemplateUtils = new RestTemplateUtils();
//			ByteArrayResource resource = new MultipartFileResource(file);
//			MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
//			param.add(fileParamName, resource);
//			log.info("请求采集服务url:{}", url);
//			restTemplateUtils.sendPostFile(url, param, request);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
}
