

package com.mawujun.http;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IP地址
 * @author mawujun
 *
 */
public class IPUtils {
	private static Logger logger = LoggerFactory.getLogger(IPUtils.class);

	/**
	 * 获取IP地址
	 * 
	 * 使用Nginx等反向代理软件， 则不能通过request.getRemoteAddr()获取IP地址
	 * 如果使用了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP地址，X-Forwarded-For中第一个非unknown的有效IP字符串，则为真实IP地址
	 */
	public static String getRequestIpAddr(HttpServletRequest request) {
    	String ip = null;
        try {
            ip = request.getHeader("x-forwarded-for");
            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (StringUtils.isEmpty(ip) || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
        } catch (Exception e) {
        	logger.error("IPUtils ERROR ", e);
        }
        
//        //使用代理，则获取第一个IP地址
//        if(StringUtils.isEmpty(ip) && ip.length() > 15) {
//			if(ip.indexOf(",") > 0) {
//				ip = ip.substring(0, ip.indexOf(","));
//			}
//		}
        
        return ip;
    }
	
	public static String[] getLocalIpAddres(boolean onlyone,boolean real) {
		try {
			List<String> ipes=new ArrayList<String>();
			Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
			while (allNetInterfaces.hasMoreElements()) {
				NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();

				// 去除回环接口，子接口，未运行和接口
				if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
					continue;
				}
//				//相当于是只获取指定网卡的ip地址
//				if (!netInterface.getDisplayName().contains("Intel")
//						&& !netInterface.getDisplayName().contains("Realtek")) {
//					continue;
//				}
				//System.out.println("DisplayName:"+netInterface.getDisplayName());
				//System.out.println("name:"+netInterface.getName());
				if (netInterface.getName().contains("docker") || netInterface.getName().contains("lo")
						||netInterface.getDisplayName().contains("VirtualBox") //排除VirtualBox虚拟机
						) {
					continue;
				}
				Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress ip = addresses.nextElement();
					if (ip != null) {
						// ipv4
						if (ip instanceof Inet4Address) {
							System.out.println("ipv4 = " + ip.getHostAddress());
							ipes.add(ip.getHostAddress());
							if(onlyone) {
								return ipes.toArray(new String[ipes.size()]);
							}
							
							//
						}
					}
				}
				//break;
			}
			if(ipes!=null && ipes.size()>0) {
				return ipes.toArray(new String[ipes.size()]);
			} else {
				return null;
			}
		} catch (SocketException e) {
			logger.error("获取真实的ip地址失败", e);
			//System.err.println("Error when getting host ip address" + e.getMessage());
		}
		return null;
	}
	/**
	 * 获取所有真实的ip地址，因为一台机器会即连wifi，也同时连又有线
	 * @return
	 */
	public static String[] getLocalIpAddres() {
		return getLocalIpAddres(false,true);
	}
	/**
	 * 获取本机的ip地址，读取到了WiFi或者有线地址其中之一立即return。
	 * @param request
	 * @return,如果没有获取导返回null
	 */
	public static String getLocalIpAddr() {
		String[] ipes= getLocalIpAddres(true,true);
			if(ipes!=null && ipes.length>0) {
				return ipes[0];
			} else {
				return null;
			}
	
//		try {
////			InetAddress addr = InetAddress.getLocalHost();
////			System.out.println("Local HostAddress:"+addr.getHostAddress());
////				      String hostname = addr.getHostName();
////				      System.out.println("Local host name: "+hostname);
//				      
//		    // 遍历所有的网络接口
//			Enumeration allNetInterfaces = NetworkInterface.getNetworkInterfaces();
//			InetAddress ip = null;
//			String ipAddr=null;
//			while (allNetInterfaces.hasMoreElements()) {
//				NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
//				String name = netInterface.getName();
//				//System.out.println(netInterface.getName());
//				//过滤掉docker和lo
//				if (name.contains("docker") || name.contains("lo")) {
//					continue;
//				}
//				
//				
//				Enumeration addresses = netInterface.getInetAddresses();
//				while (addresses.hasMoreElements()) {
//					ip = (InetAddress) addresses.nextElement();
//					// 排除loopback类型地址
//					if(ip.isLoopbackAddress()) {
//						continue;
//					}
//					if (ip != null && ip instanceof Inet4Address) {
//						System.out.println("本机的IP = " + ip.getHostAddress());
//						ipAddr=ip.getHostAddress();
//					}
//				}
//			}
//			return ipAddr;
//		} catch (SocketException e) {
//			logger.error("IPUtils ERROR ", e);
//		} catch (UnknownHostException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return null;
	}
	
}
