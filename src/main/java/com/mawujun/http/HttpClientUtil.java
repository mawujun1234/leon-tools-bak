package com.mawujun.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mawujun.util.StringUtil;
import com.mawujun.util.XmlUtil;

public class HttpClientUtil {
	private static Logger logger=LoggerFactory.getLogger(HttpClientUtil.class);
	
	private static final CloseableHttpClient httpClient;
    public static final String CHARSET = "UTF-8";
    // 采用静态代码块，初始化超时时间配置，再根据配置生成默认httpClient对象
    static {
        RequestConfig config = RequestConfig.custom().setConnectTimeout(60000).setSocketTimeout(15000).build();
        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
    }
    
    /**
	 * 获取真实的ip地址
	 * @param request
	 * @return
	 */
	public static String getIpAddr(HttpServletRequest request) {
		return IPUtils.getRequestIpAddr(request);
	}

    public static String doGet(String url, Map<String, Object> params) {
        return doGet(url, params, CHARSET);
    }

    public static String doGetSSL(String url, Map<String, Object> params) {
        return doGetSSL(url, params, CHARSET);
    }

    public static String doPost(String url, Map<String, Object> params)  {
        try {
			return doPost(url, params, CHARSET);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("请求发生异常!",e);
		}
    }
    /**
     * 
     * @param url
     * @param param json的内容
     * @return
     */
    public static String doPostJsonBody(String url,String param){
    	return doPostJsonBody( url,null,  param);
    }
    /**
     * 
     * @param url
     * @param headers 有些请求要带上自定义的头部信息
     * @param param xml的内容
     * @return
     */
    public static String doPostJsonBody(String url,Map<String, Object> headers, String param){
        return doPostBody(url,headers,ContentType.APPLICATION_JSON,param);
    }
    
    /**
     * 
     * @param url
     * @param param xml的内容
     * @return
     */
    public static String doPostXmlBody(String url,String param){
    	return doPostXmlBody( url,null,  param);
    }
    /**
     * map中只能嵌套map，list，list中也只能放map
     * @param url
     * @param rootName
     * @param data
     * @return
     */
    public static String doPostXmlBody(String url,String rootName,Map<String,Object> data){
    	try {
			return doPostXmlBody( url,null,  XmlUtil.mapToXmlStr(data, rootName));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("请求失败",e);
		}
    }
    /**
     * 
     * @param url
     * @param headers 有些请求要带上自定义的头部信息
     * @param param json的内容
     * @return
     */
    public static String doPostXmlBody(String url,Map<String, Object> headers, String param){
        return doPostBody(url,headers,ContentType.APPLICATION_XML,param);
    }
    
    public static String doPostBody(String url,Map<String, Object> headers,ContentType contentType, String param){
        HttpPost httpPost = new HttpPost(url);
        CloseableHttpClient client = HttpClients.createDefault();
        String respContent = null;
        //json方式

        StringEntity entity = new StringEntity(param,contentType);//解决中文乱码问题,自带utf-8

        //entity.setContentType("application/json; charset=UTF-8");
        //httpPost.setHeader("Accept", "application/json");

        httpPost.setEntity(entity);
        if (headers!=null){

            for (Map.Entry<String, Object> e : headers.entrySet()) {
                httpPost.setHeader(e.getKey(), e.getValue().toString());
            }
        }
        HttpResponse resp = null;
        try {
            resp = client.execute(httpPost);
            if(resp.getStatusLine().getStatusCode() == 200) {
                HttpEntity he = resp.getEntity();
                respContent = EntityUtils.toString(he,CHARSET);
            }
        } catch (IOException e) {
            logger.error("请求异常:"+url,e);
        }finally {
            try {
                client.close();
            } catch (IOException e) {
                logger.error("请求异常:"+url,e);
            }
        }
        return respContent;
    }
    
    
    public static String doPostFile(String url,String fileParamName,File file,Map<String,Object> params){
    	return doPostFileObject(url,fileParamName,file,params);
    }
    public static String doPostFile(String url,String fileParamName,byte[] file,Map<String,Object> params){
    	return doPostFileObject(url,fileParamName,file,params);
    }
    public static String doPostFile(String url,String fileParamName,InputStream file,Map<String,Object> params){
    	return doPostFileObject(url,fileParamName,file,params);
    }
    
    /**
     * 可以传递多个文件，Object 可以是File，byte[],InputStream
     * 传递额外参数，就使用其他类型,参数和接收类型对应关系如下
     * File--->MultipartFile
     * byte[]--->byte[]
     * InputStream--->InputStream
     * String---->byte[]
     * @param url
     * @param params
     * @return
     */
    public static String doPostFile(String url,Map<String,Object> params){
    	HttpPost httpPost = new HttpPost(url);
        CloseableHttpClient client = HttpClients.createDefault();
        String respContent = null;
        
        MultipartEntityBuilder multipartEntityBuilder=MultipartEntityBuilder.create();

    
//        MultipartEntityBuilder multipartEntityBuilder=MultipartEntityBuilder.create()
//        		.addBinaryBody(fileParamName, file);
        if(params!=null) {
        	for(Entry<String,Object> entry:params.entrySet()) {
        		// StringBody comment = new StringBody(paramContent, ContentType.TEXT_PLAIN);
        		addMultipartEntityBuilder(multipartEntityBuilder,entry.getKey(),entry.getValue());
        	}
        }
        HttpEntity entity = multipartEntityBuilder.build();
        


        httpPost.setEntity(entity);

        HttpResponse resp = null;
        try {
            resp = client.execute(httpPost);
            if(resp.getStatusLine().getStatusCode() == 200) {
                HttpEntity he = resp.getEntity();
                respContent = EntityUtils.toString(he,CHARSET);
            }
        } catch (IOException e) {
            logger.error("请求异常:"+url,e);
        }finally {
            try {
                client.close();
            } catch (IOException e) {
                logger.error("请求异常:"+url,e);
            }
        }
        return respContent;
    }
    
    
    private static MultipartEntityBuilder addMultipartEntityBuilder(MultipartEntityBuilder multipartEntityBuilder,String fileParamName, Object obj) {
    	
    	if(obj instanceof File) {
    		multipartEntityBuilder.addBinaryBody(fileParamName, (File)obj);
    	} else if(obj instanceof byte[]) {
    		multipartEntityBuilder.addBinaryBody(fileParamName, (byte[])obj);
    	} else if(obj instanceof InputStream) {
    		multipartEntityBuilder.addBinaryBody(fileParamName, (InputStream)obj);
    	} else if(obj instanceof String) {
    		multipartEntityBuilder.addTextBody(fileParamName, (String)obj);
    	} else  {
    		multipartEntityBuilder.addTextBody(fileParamName, obj.toString());
    	}
    	return multipartEntityBuilder;
    }
    /**
                 * 接收方式@RequestParam("fileParamName") MultipartFile fileParamName
     * 
     * @param url
     * @param fileParamName 文件参数的名字
     * @param file 文件内容
     * @param params 其他参数
     * @return
     */
    private static String doPostFileObject(String url,String fileParamName,Object file,Map<String,Object> params){
        HttpPost httpPost = new HttpPost(url);
        CloseableHttpClient client = HttpClients.createDefault();
        String respContent = null;
        
        
//        //参数用map，里面的内容可以是，String、File以及InputStream对应的ContentBody类型的子类，如FileBody、InputStreamBody、StringBody，通过这些类我们可以将String、File以及InputStream类型的数据转换成ContentBody类型的数据
//        FileBody bin = new FileBody(file);
//        FormBodyPart formBodyPart=FormBodyPartBuilder.create(fileParam,bin)
//        		.build();
        MultipartEntityBuilder multipartEntityBuilder=MultipartEntityBuilder.create();
        addMultipartEntityBuilder(multipartEntityBuilder,fileParamName,file);
    
//        MultipartEntityBuilder multipartEntityBuilder=MultipartEntityBuilder.create()
//        		.addBinaryBody(fileParamName, file);
        if(params!=null) {
        	for(Entry<String,Object> entry:params.entrySet()) {
        		// StringBody comment = new StringBody(paramContent, ContentType.TEXT_PLAIN);
        		multipartEntityBuilder.addTextBody(entry.getKey(), entry.getValue().toString());
        	}
        }
        HttpEntity entity = multipartEntityBuilder.build();
        


        httpPost.setEntity(entity);

        HttpResponse resp = null;
        try {
            resp = client.execute(httpPost);
            if(resp.getStatusLine().getStatusCode() == 200) {
                HttpEntity he = resp.getEntity();
                respContent = EntityUtils.toString(he,CHARSET);
            }
        } catch (IOException e) {
            logger.error("请求异常:"+url,e);
        }finally {
            try {
                client.close();
            } catch (IOException e) {
                logger.error("请求异常:"+url,e);
            }
        }
        return respContent;
    }
    
    protected static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        documentBuilderFactory.setXIncludeAware(false);
        documentBuilderFactory.setExpandEntityReferences(false);

        return documentBuilderFactory.newDocumentBuilder();
    }
 


    /**
     * HTTP Get 获取内容
     * @param url 请求的url地址 ?之前的地址
     * @param params 请求的参数
     * @param charset 编码格式
     * @return 页面内容
     */
    public static String doGet(String url, Map<String, Object> params, String charset) {
        if (StringUtil.isBlank(url)) {
            return null;
        }
        try {
            if (params != null && !params.isEmpty()) {
                List<NameValuePair> pairs = new ArrayList<NameValuePair>(params.size());
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    String value = entry.getValue().toString();
                    if (value != null) {
                        pairs.add(new BasicNameValuePair(entry.getKey(), value));
                    }
                }
                // 将请求参数和url进行拼接
                url += "?" + EntityUtils.toString(new UrlEncodedFormEntity(pairs, charset));
            }
            HttpGet httpGet = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                httpGet.abort();
                throw new RuntimeException("HttpClient,error status code :" + statusCode);
            }
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, CHARSET);
            }
            EntityUtils.consume(entity);
            response.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * HTTP Post 获取内容
     * @param url 请求的url地址 ?之前的地址
     * @param params 请求的参数
     * @param charset 编码格式
     * @return 页面内容
     * @throws IOException
     */
    public static String doPost(String url, Map<String, Object> params, String charset) 
            throws IOException {
        if (StringUtil.isBlank(url)) {
            return null;
        }
        List<NameValuePair> pairs = null;
        if (params != null && !params.isEmpty()) {
            pairs = new ArrayList<NameValuePair>(params.size());
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String value = entry.getValue().toString();
                if (value != null) {
                    pairs.add(new BasicNameValuePair(entry.getKey(), value));
                }
            }
        }
        HttpPost httpPost = new HttpPost(url);
        if (pairs != null && pairs.size() > 0) {
            httpPost.setEntity(new UrlEncodedFormEntity(pairs, CHARSET));
        }
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                httpPost.abort();
                throw new RuntimeException("HttpClient,error status code :" + statusCode);
            }
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, CHARSET);
            }
            EntityUtils.consume(entity);
            return result;
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            if (response != null)
                response.close();
        }
        return null;
    }

    /**
     * HTTPS Get 获取内容
     * @param url 请求的url地址 ?之前的地址
     * @param params 请求的参数
     * @param charset  编码格式
     * @return 页面内容
     */
    public static String doGetSSL(String url, Map<String, Object> params, String charset) {
        if (StringUtil.isBlank(url)) {
            return null;
        }
        try {
            if (params != null && !params.isEmpty()) {
                List<NameValuePair> pairs = new ArrayList<NameValuePair>(params.size());
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    String value = entry.getValue().toString();
                    if (value != null) {
                        pairs.add(new BasicNameValuePair(entry.getKey(), value));
                    }
                }
                url += "?" + EntityUtils.toString(new UrlEncodedFormEntity(pairs, charset));
            }
            HttpGet httpGet = new HttpGet(url);

            // https  注意这里获取https内容，使用了忽略证书的方式，当然还有其他的方式来获取https内容
            CloseableHttpClient httpsClient = HttpClientUtil.createSSLClientDefault();
            CloseableHttpResponse response = httpsClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                httpGet.abort();
                throw new RuntimeException("HttpClient,error status code :" + statusCode);
            }
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, CHARSET);
            }
            EntityUtils.consume(entity);
            response.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 这里创建了忽略整数验证的CloseableHttpClient对象
     * @return
     */
    public static CloseableHttpClient createSSLClientDefault() {
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                // 信任所有
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
            return HttpClients.custom().setSSLSocketFactory(sslsf).build();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return HttpClients.createDefault();
    }

}
