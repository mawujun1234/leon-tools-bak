package com.mawujun.sso;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class RedirectUtil {
	private static  Map<String, String> hostes=new HashMap<>();
	static String SERVER_CHARSET;
	
	public final static String Cloud_host="10.22.1.235";
	static {
		hostes.put("ssocloud", "http://"+Cloud_host+":8080");
		hostes.put("ssotest", "http://192.168.196.220:8080");
	}
	
	public static String getHost(String type,String uri) {
		String host=hostes.get(type);
		return host+uri;
	}
//	 public static boolean DEBUG;
//
//	    public static String getServerHost() {
//	        return SERVER_HOST;
//	    }
//
//	    static String SERVER_HOST;
//	    static int SERVER_PORT;
//	    static String SERVER_PROTOCOL;
//	    static String SERVER_CHARSET;
//	    static Map<String, String> hostPrefixMap;
//
//	    static {
////	        ResourceBundle bundle = ResourceBundle.getBundle("server");
////	        SERVER_HOST = bundle.getString("host");
////	        SERVER_PORT = Integer.parseInt(bundle.getString("port"));
////	        SERVER_PROTOCOL = bundle.getString("protocol");
////	        SERVER_CHARSET = bundle.getString("charset");
////	        DEBUG = SERVER_HOST.equals("localhost");
////	        hostPrefixMap = new HashMap<String, String>();
////	        hostPrefixMap.put("user", bundle.getString("user"));
////	        hostPrefixMap.put("algorithm", bundle.getString("algorithm"));
////	        hostPrefixMap.put("image", bundle.getString("image"));
//	//
//	        SERVER_HOST = "192.168.196.220";
//	        SERVER_PORT = 8080;
//	        SERVER_PROTOCOL = "http";
////	        SERVER_CHARSET = bundle.getString("charset");
//	        DEBUG = SERVER_HOST.equals("localhost");
//	        hostPrefixMap = new HashMap<String, String>();
//	        hostPrefixMap.put("relay", "relay");
//	    }
//
//	    //获取是否为本地调试
//	    public static Boolean getDebug(){
//	        return DEBUG;
//	    }
//
//	    public static String getHost(String hostPrefix) {
//	        return getHost(hostPrefix, "");
//	    }
//
//	    private static String getHost(String hostPrefix, String url) {
////	        hostPrefix = hostPrefixMap.get(hostPrefix);//例如: trade
//	        String host = SERVER_HOST;
//	        if (hostPrefix != null && !hostPrefix.isEmpty()) {
//	            url = hostPrefix;//例如: / + image + /image/report/getReportList
//	        }
//	        try {
//	            return new URL(SERVER_PROTOCOL, host, SERVER_PORT, url).toString();
//	        } catch (MalformedURLException e) {
//	            e.printStackTrace();
//	            return SERVER_PROTOCOL + "://" + host + ":" + SERVER_PORT + "url";
//	        }
//	    }
}
