package com.mawujun.thread.http;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 一个地址一个http请求池，绑定在一起了，所以defaultMaxPerRoute和maxTotal设置成一样，
 * 因为这是根据业务情况决定的，高并发请求的时候，基本就是那么几个地址，而这几个地址的个数是有限的，所以做这个决定
 * @author admin
 *
 */
public class HttpClientPoolUtil {
	private Logger logger = LoggerFactory.getLogger(HttpClientPoolUtil.class);

	protected PoolingHttpClientConnectionManager cm = null;

	//protected CloseableHttpClient httpClient = null;

	/**
	 * 默认content 类型
	 */
	protected String DEFAULT_CONTENT_TYPE = "application/json";

	protected int defaultMaxPerRoute = 50;// 每个route默认的连接数
	protected int maxTotal = defaultMaxPerRoute*2;// 整个连接池的最大连接数
	/**
	 * 默认请求超时时间60s
	 */
	protected int defaul_time_out = 60;
	protected int http_default_keep_time = 60;

	
	//线程池
	protected int thread_pool_size=defaultMaxPerRoute;
	protected ExecutorService fixedThreadPool = null;


////    //关闭空闲多长时间的请求
////    private static  int idel_timeout=(http_default_keep_time+20)*1000;
//	// 多久检查一次
//	private int monitor_interval = 10 * 1000;
//	private ScheduledExecutorService monitorExecutor = null;// Executors.newScheduledThreadPool(1);
	
//	/**
//	 * 定时关闭失效的链接
//	 */
//	private void clearInvalidConnection() {
//		//System.out.println(cm.getTotalStats().getAvailable() + "----");
//		//monitorExecutor = Executors.newScheduledThreadPool(1);
//		if(cm==null) {
//			return;
//		}
//		monitorExecutor.scheduleAtFixedRate(new TimerTask() {
//			@Override
//			public void run() {
//				System.out.println(cm.getTotalStats().getAvailable() + "----");
//				// 关闭异常连接
//				cm.closeExpiredConnections();
//				// 关闭5s空闲的连接
//				cm.closeIdleConnections((http_default_keep_time + 20) * 1000, TimeUnit.MILLISECONDS);
//				logger.info("关闭过期或者空闲超过" + monitor_interval + "毫秒的数据");
//			}
//		}, 500, monitor_interval, TimeUnit.MILLISECONDS);
//	}
	/**
	 * 关闭线程池，并且不再接收新的任务了
	 * 不能用来统计任务时长
	 */
	public void shutdown() {
		this.fixedThreadPool.shutdown();
		
	}
	
	/**
	 * 不再接收新的线程任务了
	 * 会阻塞直到所有的线程执行完毕，可以用来统计运行时长
	 * @return true表示所有线程都执行完毕了，正常结束，false表示线程超时停止
	 * @throws InterruptedException
	 */
	public boolean shutdownLatch() throws InterruptedException {
		shutdown();
		boolean bool=true;
		try {
			
			bool= this.fixedThreadPool.awaitTermination(1, TimeUnit.HOURS);
			
		}  finally {
			this.cm.close();
//			try {
//				//httpClient.close();
//				this.cm.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
		}
		return bool;
	}

	private static SSLContext createSSLContext() {
		SSLContext sslcontext = null;
		try {
			sslcontext = SSLContext.getInstance("TLS");
			sslcontext.init(null, new TrustManager[] { new TrustAnyTrustManager() }, new SecureRandom());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
		return sslcontext;
	}

	private static class TrustAnyTrustManager implements X509TrustManager {

		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[] {};
		}
	}

	private Registry<ConnectionSocketFactory> getRegistry() {
		SSLContext sslcontext = createSSLContext();
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new HostnameVerifier() {

			@Override
			public boolean verify(String paramString, SSLSession paramSSLSession) {
				return true;
			}
		});

		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("https", sslsf).register("http", new PlainConnectionSocketFactory()).build();
		return socketFactoryRegistry;

	}

	/**
	 * 默认是50个请求并发执行
	 */
	public  static synchronized HttpClientPoolUtil initPools() {
		HttpClientPoolUtil pool=new HttpClientPoolUtil();
		
		

		PoolingHttpClientConnectionManager cm=new PoolingHttpClientConnectionManager(pool.getRegistry());
		pool.cm=cm;
		cm.setDefaultMaxPerRoute(pool.getDefaultMaxPerRoute());
		cm.setMaxTotal(pool.getMaxTotal());
		//CloseableHttpClient httpClient = HttpClients.custom().setKeepAliveStrategy(pool.defaultStrategy).setConnectionManager(cm).build();
		//pool.httpClient=httpClient;
		//clearInvalidConnection();
		
		
		pool.fixedThreadPool=Executors.newFixedThreadPool(pool.getThread_pool_size());
		return pool;
	}

	/**
	 * 整个系统中，只有第一次调用的时候有效
	 * @param defaultMaxPerRoute 整个连接池的最大连接数,默认50
	 * @param maxTotal 每个route默认的连接数，默认defaultMaxPerRoute*2
	 * @param thread_pool_size 线程池额数量 默认等于defaultMaxPerRoute
	 */
	public static synchronized HttpClientPoolUtil initPools(int defaultMaxPerRoute,int maxTotal,int thread_pool_size) {
		HttpClientPoolUtil pool=new HttpClientPoolUtil();
		
		pool.defaultMaxPerRoute=defaultMaxPerRoute;
		pool.maxTotal=maxTotal;
		pool.thread_pool_size=thread_pool_size;
		
		
		PoolingHttpClientConnectionManager cm=new PoolingHttpClientConnectionManager(pool.getRegistry());
		cm.setDefaultMaxPerRoute(pool.getDefaultMaxPerRoute());
		cm.setMaxTotal(pool.getMaxTotal());
		//CloseableHttpClient httpClient = HttpClients.custom().setKeepAliveStrategy(pool.defaultStrategy).setConnectionManager(cm).build();
		//pool.httpClient=httpClient;
		//clearInvalidConnection();
		pool.cm=cm;
		
		pool.fixedThreadPool=Executors.newFixedThreadPool(pool.getThread_pool_size());
		return pool;
		
	}

	/**
	 * Http connection keepAlive 设置
	 */
	public ConnectionKeepAliveStrategy defaultStrategy = new ConnectionKeepAliveStrategy() {
		public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
			HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
			int keepTime = http_default_keep_time;
			while (it.hasNext()) {
				HeaderElement he = it.nextElement();
				String param = he.getName();
				String value = he.getValue();
				if (value != null && param.equalsIgnoreCase("timeout")) {
					try {
						return Long.parseLong(value) * 1000;
					} catch (Exception e) {
						e.printStackTrace();
						logger.error("格式化 KeepAlive 过期时间一长:" + e.toString());
					}
				}
			}
			return keepTime * 1000;
		}
	};

//	public  CloseableHttpClient getHttpClient() {
//		return httpClient;
//	}

	public PoolingHttpClientConnectionManager getHttpConnectionManager() {
		return cm;
	}

	/**
	 * 以body的形式传递数据导后台
	 * @param uri
	 * @param data 是json的字符串
	 * @param callback
	 */
	public void postJson(String uri, String data,IHttpClientPoolCallback<?> callback) {
		
		fixedThreadPool.execute(new Runnable() {
			public void run() {
				postJsonNoThread(uri,data,callback);
			}
		});
	}

	/**
	 * 执行http post请求 默认采用Content-Type：application/json，Accept：application/json
	 * 把数据以body的形式传递到后台
	 * 
	 * @param uri  请求地址
	 * @param data 请求数据
	 * @return
	 */
	protected int postJsonNoThread(String uri, String data,IHttpClientPoolCallback<?> callback) {
		long startTime = System.currentTimeMillis();
		//HttpEntity httpEntity = null;
		HttpEntityEnclosingRequestBase method = null;
		CloseableHttpResponse httpResponse =null;
		//String responseBody = null;
		int result=0;
		try {
			if (cm == null) {
				initPools();
			}
			method = (HttpEntityEnclosingRequestBase) getRequest(uri, HttpPost.METHOD_NAME, DEFAULT_CONTENT_TYPE, 0);
			callback.requestSetup(method);
			
			method.setEntity(new StringEntity(data));
			HttpContext context = HttpClientContext.create();
			CloseableHttpClient httpClient = HttpClients.custom().setKeepAliveStrategy(this.defaultStrategy).setConnectionManager(cm).build();
			
			httpResponse = httpClient.execute(method, context);
			if(httpResponse.getStatusLine().getStatusCode()==200) {
//				httpEntity = httpResponse.getEntity();
//				if (httpEntity != null) {
//					responseBody = EntityUtils.toString(httpEntity, "UTF-8");
//				}
				
				result= callback.success(httpResponse);
			} else {
				result= callback.failure(httpResponse);
			}
			
		} catch (Exception e) {
			if (method != null) {
				method.abort();
			}
			e.printStackTrace();
			logger.error("请求发生异常, url:" + uri + ", exception:" + e.toString()
					+ ", cost time(ms):" + (System.currentTimeMillis() - startTime));
			result= callback.exception(e);
		} finally {
			if (httpResponse != null) {
				try {
					//httpResponse.close();
					EntityUtils.consume(httpResponse.getEntity());

				} catch (IOException e) {
					e.printStackTrace();
					logger.error("关闭响应发生异常, url:" + uri + ", exception:" + e.toString() + ", cost time(ms):"
							+ (System.currentTimeMillis() - startTime));
				}
				
			}
		}
		//return responseBody;
		return result;
	}

	/**
	 * 发送数据，以表单的形式发送数据
	 * @param uri
	 * @param params
	 * @param callback
	 */
	public void postForm(String uri, Map<String, Object> params,IHttpClientPoolCallback<?> callback) {
		
		fixedThreadPool.execute(new Runnable() {
			public void run() {
				postFormNoThread(uri,params,callback);
			}
		});
	}
	


	/**
	 * 通过form表单的形式给后台传递数据
	 * 
	 * @param uri
	 * @param params
	 * @return
	 * @throws IOException 
	 */
	protected int postFormNoThread(String uri, Map<String, Object> params,IHttpClientPoolCallback<?> callback) {

		long startTime = System.currentTimeMillis();
		//HttpEntity httpEntity = null;
		CloseableHttpResponse httpResponse =null;
		HttpEntityEnclosingRequestBase method = null;
		//String responseBody = null;
		int result=0;
		try {
			if (cm == null) {
				initPools();
			}
			method = (HttpEntityEnclosingRequestBase) getRequest(uri, HttpPost.METHOD_NAME,"application/x-www-form-urlencoded; charset=UTF-8", 0);
			callback.requestSetup(method);
			
			if (params != null && params.size() != 0) {
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				for (Entry<String, Object> entry : params.entrySet()) {
					nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
				}
				method.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
			}

			HttpContext context = HttpClientContext.create();
			CloseableHttpClient httpClient = HttpClients.custom().setKeepAliveStrategy(this.defaultStrategy).setConnectionManager(cm).build();
			
			httpResponse = httpClient.execute(method, context);
//			httpEntity = httpResponse.getEntity();
//			if (httpEntity != null) {
//				responseBody = EntityUtils.toString(httpEntity, "UTF-8");
//			}
			if(httpResponse.getStatusLine().getStatusCode()==200) {
				
				result= callback.success(httpResponse);
			} else {
				logger.error(httpResponse.toString());
				result= callback.failure(httpResponse);
			}
			httpResponse.close();
		} catch (Exception e) {
			if (method != null) {
				method.abort();
			}
			e.printStackTrace();
			logger.error("关闭响应发生异常, url:" + uri + ", exception:" + e.toString()
					+ ", cost time(ms):" + (System.currentTimeMillis() - startTime));
			callback.exception(e);
		} finally {
			if (httpResponse != null) {
				try {
					EntityUtils.consume(httpResponse.getEntity());
				} catch (IOException e) {
					e.printStackTrace();
					logger.error("关闭响应发生异常, url:" + uri + ", exception:" + e.toString() + ", cost time(ms):"
							+ (System.currentTimeMillis() - startTime));
				}
				
			}
		}
		return result;

	}

	public void get(String uri, Map<String, Object> params,IHttpClientPoolCallback<?> callback) {
		
		fixedThreadPool.execute(new Runnable() {
			public void run() {
				getNoThread(uri,params,callback);
			}
		});
	}

	/**
	 * 执行GET 请求
	 *
	 * @param uri
	 * @return
	 */
	protected int getNoThread(String uri, Map<String, Object> param,IHttpClientPoolCallback<?> callback) {
		long startTime = System.currentTimeMillis();
		//HttpEntity httpEntity = null;
		CloseableHttpResponse httpResponse=null;
		HttpRequestBase method = null;
		//String responseBody =null;
		int result=0;
		try {
			if (cm == null) {
				initPools();
			}
			if (param != null && param.size() != 0) {
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				for (Entry<String, Object> entry : param.entrySet()) {
					nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
				}
				String str = EntityUtils.toString(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
				if (uri.indexOf('?') != -1) {
					uri = uri + "&" + str;
				} else {
					uri = uri + "?" + str;
				}
			}
			method = getRequest(uri, HttpGet.METHOD_NAME, DEFAULT_CONTENT_TYPE, 0);
			callback.requestSetup(method);

			HttpContext context = HttpClientContext.create();
			CloseableHttpClient httpClient = HttpClients.custom().setKeepAliveStrategy(this.defaultStrategy).setConnectionManager(cm).build();
			httpResponse = httpClient.execute(method, context);
//			httpEntity = httpResponse.getEntity();
//			if (httpEntity != null) {
//				responseBody = EntityUtils.toString(httpEntity, "UTF-8");
//				logger.info("请求URL: " + uri + "+  返回状态码：" + httpResponse.getStatusLine().getStatusCode());
//			}
			if(httpResponse.getStatusLine().getStatusCode()==200) {
				
				result= callback.success(httpResponse);
			} else {
				result= callback.failure(httpResponse);
			}
			httpResponse.close();
		} catch (Exception e) {
			if (method != null) {
				method.abort();
			}
			e.printStackTrace();
			logger.error("执行请求发生异常, url:" + uri + ", exception:" + e.toString() + ", cost time(ms):"
					+ (System.currentTimeMillis() - startTime));
			result= callback.exception(e);
		} finally {
			if (httpResponse != null) {
				try {
					EntityUtils.consume(httpResponse.getEntity());
				} catch (IOException e) {
					e.printStackTrace();
					logger.error("关闭响应发生异常, url:" + uri + ", exception:" + e.toString() + ", cost time(ms):"
							+ (System.currentTimeMillis() - startTime));
				}
				
			}
		}
		return result;
	}
	
	/**
	 * 创建请求
	 *
	 * @param uri         请求url
	 * @param methodName  请求的方法类型
	 * @param contentType contentType类型
	 * @param timeout     超时时间 s
	 * @return
	 */
	public HttpRequestBase getRequest(String uri, String methodName, String contentType, int timeout) {
		if (cm == null) {
			initPools();
		}
		HttpRequestBase method = null;
		if (timeout <= 0) {
			timeout = defaul_time_out;
		}
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeout * 1000)
				.setConnectTimeout(timeout * 1000).setConnectionRequestTimeout(timeout * 1000)
				.setExpectContinueEnabled(false).build();

		if (HttpPut.METHOD_NAME.equalsIgnoreCase(methodName)) {
			method = new HttpPut(uri);
		} else if (HttpPost.METHOD_NAME.equalsIgnoreCase(methodName)) {
			method = new HttpPost(uri);
		} else if (HttpGet.METHOD_NAME.equalsIgnoreCase(methodName)) {
			method = new HttpGet(uri);
		} else {
			method = new HttpPost(uri);
		}
		if (StringUtils.isBlank(contentType)) {
			contentType = DEFAULT_CONTENT_TYPE;
		}
		method.addHeader("Content-Type", contentType);
		method.addHeader("Accept", contentType);
		method.setConfig(requestConfig);
		return method;
	}
	
	public int getHttpDefaultKeepTime() {
		return http_default_keep_time;
	}

	public int getDefaultMaxPerRoute() {
		return defaultMaxPerRoute;
	}
	public int getMaxTotal() {
		return maxTotal;
	}
	public int getThread_pool_size() {
		return thread_pool_size;
	}
	public ExecutorService getFixedThreadPool() {
		return fixedThreadPool;
	}

}
