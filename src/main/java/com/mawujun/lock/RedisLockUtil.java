package com.mawujun.lock;

import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SentinelServersConfig;
import org.redisson.config.SingleServerConfig;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author mawujun 16064988 16064988@qq.com
 * @date 2020-07-16
 **/
public class RedisLockUtil {
    private static RedissonClient client;
    // private static final String LOCK_TITLE = "TrackSaveLock_";

    private static boolean isdev=false;
    private static Map<String,Long> devLock=new LinkedHashMap<String,Long>();


    /**
     * 哨兵模式
     * @param sentinelAddresses
     * @param masterName
     * @param timeout
     * @param masterPoolSize
     * @param slavePoolSize
     * @param password
     * @return
     */
    public synchronized static RedissonClient initSentinel(String sentinelAddresses,String masterName
            ,int timeout,int masterPoolSize,int slavePoolSize,String password) {
        if (client != null) {
            return client;
        }
        Config config = new Config();
        SentinelServersConfig serverConfig = config.useSentinelServers().addSentinelAddress(sentinelAddresses)
                .setMasterName(masterName)
                .setTimeout(timeout)
                .setMasterConnectionPoolSize(masterPoolSize)
                .setSlaveConnectionPoolSize(slavePoolSize);

        if(StringUtils.isNotBlank(password)) {
            serverConfig.setPassword(password);
        }
        client = Redisson.create(config);
        return client;
    }
    /**
     * 单机模式初始化
     * @param host
     * @param database 一般使用0
     * @param timeout
     * @param poolsize
     * @param minimumIdleSize
     * @param password 可以为null，表是不使用密码
     * @return
     */
    public synchronized static RedissonClient initSingle(String host,int database,int timeout
            ,int poolsize,int minimumIdleSize,String password) {
        if (client != null) {
            return client;
        }

        Config config = new Config();
        SingleServerConfig serverConfig = config.useSingleServer()
                .setAddress(host)
                .setDatabase(database)
                .setTimeout(timeout)
                .setConnectionPoolSize(poolsize)
                .setConnectionMinimumIdleSize(minimumIdleSize);

        if(StringUtils.isNotBlank(password)) {
            serverConfig.setPassword(password);
        }
        client = Redisson.create(config);
        return client;


//        Preference prefence = ContextManager.current().get(Preference.class);
//        String url = prefence.getString("redis.default.server.url");
//        if (url == null || "".equals(url)) {
//            //throw new RuntimeException("请配置redis的地址:redis.default.server.url");豆腐干豆腐干
//            if (url == null || "".equals(url)) {
//                String config_dev = prefence.getString("config.dev");
//                if(config_dev!=null && "true".equals(config_dev)) {
//                    isdev=true;
//                    return null;
//                } else {
//                    throw new RuntimeException("请配置redis的地址:redis.default.server.url");
//                }
//            }
//        }
//
//        Config config = new Config();
//        config.useSingleServer().setAddress(url);
//        // config.useSingleServer().setPassword("redis1234");
//
//        client = Redisson.create(config);
//        return client;
    }

    protected static RedissonClient init(){
        if (client != null) {
            return client;
        } else {
            throw new RuntimeException("没有对Redis连接进行初始化，请先进行初始化!");
        }
    }


//    public static void lock(String title, Object id) {
//        RedissonClient client = init();
//
//        if(isdev) {
//            String key=title + id;
//            try {
//                while(devLock.containsKey(key)) {
//                    Thread.sleep(500);
//                }
//            } catch (InterruptedException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//            devLock.put(key, System.currentTimeMillis());
//            return;
//        }
//
//        RLock lock = client.getLock(title + id);
//        lock.lock(3, TimeUnit.SECONDS);
//    }
    /**
     * 默认锁的时长是30分钟
     * @param title
     * @param id
     */
    public static void lock30M(String title, Object id) {
        lock(title,id,30);
    }
    /**
     * 要保证title+id是唯一的,使用方法如下
     * try {
     LockHandler.lock("title", key);

     .........
     } finally {
     LockHandler.unlock("title", key);
     }
     * @param title 前缀
     * @param id 胃一值
     */
    public static void lock(String title, Object id,int minutes) {
        lock( title, id,minutes,TimeUnit.MINUTES);
    }

    /**
     *要保证title+id是唯一的,使用方法如下
     *      * try {
     *      LockHandler.lock("title", key);
     *
     *      .........
     *      } finally {
     *      LockHandler.unlock("title", key);
     *      }
     * @param title
     * @param id
     * @param lng
     * @param timeUnit
     */
    public static void lock(String title, Object id,int lng,TimeUnit timeUnit) {
        RedissonClient client = init();

        if(isdev) {
            String key=title + id;
            try {
                while(devLock.containsKey(key)) {
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            devLock.put(key, System.currentTimeMillis());
            return;
        }

        RLock lock = client.getLock(title + id);
        lock.lock(lng, timeUnit);
    }
    /**
     *
     * @param title
     * @param id
     * @return true:表示已经锁住了，false表示还没有所著
     */
    public static boolean isLocked(String title, Object id) {
        RedissonClient client = init();

        if(isdev) {
            return devLock.containsKey(title + id);
        }

        RLock mylock = client.getLock(title + id);
        // 释放锁（解锁）
        return mylock.isLocked();
    }
    /**
     * 要保证title+id是唯一的
     * @param title 前缀
     * @param id 胃一值
     */
    public static void unlock(String title, Object id) {
        RedissonClient client = init();

        if(isdev) {
            devLock.remove(title + id);
            return;
        }

        RLock mylock = client.getLock(title + id);
        if (mylock.isLocked()) {
            if (mylock.isHeldByCurrentThread()) {
                // 释放锁（解锁）
                mylock.unlock();
            }
        }
    }

//	public static void run( Random random,Map<String,Integer> result,Set<Integer> set,int id) {
//		//String key=UUID.randomUUID().toString();
//		String title="1";
//		int value=id%20;
//		LockHandler.lock(title,value);
//		try {
//
//			//result.add(id);
//			if(result.get(title+value)==null) {
//				result.put(title+value,0);
//			}
//			result.put(title+value, result.get(title+value)+1);
//			set.add(result.get(title+value));
//			System.out.println("执行业务:"+result.get(title+value)+"===="+title+"--"+value);
//
//			//Thread.sleep(random.nextInt(1000));
////		} catch (InterruptedException e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
//		} finally{
//			LockHandler.unlock(title, value);
//		}
//
//	}
//	public static void main(String args[]) throws InterruptedException {
//		Config config = new Config();
//		config.useSingleServer().setAddress("redis://192.168.196.220:6379/2");
//		// config.useSingleServer().setPassword("redis1234");
//
//		client = Redisson.create(config);
//		final Random random=new Random();
//		final Map<String,Integer> result=new HashMap<String,Integer>();
//		Set<Integer> set=new HashSet<Integer>();
//		result.put("aaa",0);//result.add(2);
//		for (int i = 0; i < 1000; i++) {
//			final int id =i;
//			Thread a = new Thread() {
//				public void run() {
//					LockHandler.run(random, result, set, id);
//
//				}
//			};
//			a.start();
//		}
//		Thread.sleep(5000);
//		System.out.println("set.size="+set.size());
//		//平均分配值，每个都为for循环除以run方法中的取模的余数。
//		for(Entry<String,Integer> entry:result.entrySet()) {
//			System.out.println(entry.getKey()+"==="+entry.getValue());
//		}
//	}
}
