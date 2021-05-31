package com.mawujun.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 使用方法;
 * ThreadPoolUtils pool=ThreadPoolUtils.initPools(3);
 * InitWaybillThread billThread = new InitWaybillThread(logistics, success_count, total);
 * pool.execute(billThread);
 * pool.shutdownLatch();//等线程结束
 * @author mawujun
 *
 */
public class ThreadPoolUtils {
	
	protected int thread_pool_size=10;
	protected ExecutorService fixedThreadPool = null;
	
	public void shutdown() {
		this.fixedThreadPool.shutdown();
		
	}
	
	/**
	 * 不再接收新的线程任务了
	 * 会阻塞直到所有的线程执行完毕，可以用来统计运行时长
	 * @return true表示所有线程都执行完毕了，正常结束，false表示线程超时停止
	 * @throws InterruptedException
	 */
	public boolean shutdownLatch() {
		shutdown();
		boolean bool=true;
		try {
			
			bool= this.fixedThreadPool.awaitTermination(1, TimeUnit.HOURS);
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  finally {

			
		}
		return bool;
	}
	
	/**
	 * 不再接收新的线程任务了
	 * 会阻塞直到所有的线程执行完毕，可以用来统计运行时长
	 * @return true表示所有线程都执行完毕了，正常结束，false表示线程超时停止
	 * @param timeout
	 * @param unit
	 * @return
	 */
	public boolean shutdownLatch(int timeout, TimeUnit unit) {
		shutdown();
		boolean bool=true;
		try {
			
			bool= this.fixedThreadPool.awaitTermination(1, TimeUnit.HOURS);
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  finally {

			
		}
		return bool;
	}
	
	
	public  static synchronized ThreadPoolUtils initPools() {
		ThreadPoolUtils pool=new ThreadPoolUtils();
		pool.fixedThreadPool=Executors.newFixedThreadPool(pool.getThread_pool_size());
		return pool;
	}
	
	public  static synchronized ThreadPoolUtils initPools(int thread_pool_size) {
		
		ThreadPoolUtils pool=new ThreadPoolUtils();
		pool.thread_pool_size=thread_pool_size;
		pool.fixedThreadPool=Executors.newFixedThreadPool(pool.getThread_pool_size());
		return pool;
	}

	public int getThread_pool_size() {
		return thread_pool_size;
	}

	
	public void execute(Runnable runnable) {
		
		fixedThreadPool.execute(runnable);
	}

}
