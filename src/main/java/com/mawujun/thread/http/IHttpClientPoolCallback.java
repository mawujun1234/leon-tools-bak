package com.mawujun.thread.http;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;

/**
 * 线程池回调函数,实现这个接口是线程不安全的，因为要调用getResult
 * 所以最佳实践是一个线程一个回调函数
 * @author admin
 *
 */
public interface IHttpClientPoolCallback<T> {
	 /**
	  * 返回Http状态200 OK的调用, 根据协议要求处理，如果失败请返回负数<br>
	  * String result = EntityUtils.toString(resp.getEntity(),ReqUtils.DEFAULT_ENCODING);
	  * @param resp
	  * @return
	  */
	 public int success(HttpResponse httpResponse)throws IOException;
	 
	 /**
	  * 当连接失败或Http返回状态码非200 OK时调用
	  * @param resp
	  * @return
	  */
	 public int failure(HttpResponse httpResponse)throws IOException;
	 
	 /**
	  * 发生异常的时候，例如网络不通的时候
	  * @param e
	  * @return
	  */
	 public int exception(Exception e);
	 
	 /**
	  * 用于succss事件中处理结果的返回
	  * @return String
	  */
	 public  T getResult();

	 /**
	  * 传入的参数可能是HttpGet或HttpPost，用于在发送数据前的初始化操作
	  * 一般不需要设置，
	  * @param httpRequest as HttpRequestBase
	  */
	 public void requestSetup(HttpRequestBase request);
	 
}
