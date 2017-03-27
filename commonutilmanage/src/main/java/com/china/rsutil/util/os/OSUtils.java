package com.china.rsutil.util.os;  

import org.apache.log4j.Logger;

import com.china.rsutil.util.exception.ExceptionUtils;

/** 
 *@Title OSUtils.java 
 *@description TODO 
 *@time 2017年3月7日 下午3:26:57 
 *@author kakasun 
 **/
public class OSUtils {
	
	private static Logger logger = Logger.getLogger(OSUtils.class);
	
	/**
	 * 获取系统类型
	 * @return 返回系统类型的名称
	 */
	public static String getOS() {
		return System.getProperty("os.name");
	}
	
	/**
	 * 获取当前系统时间作为文件名
	 * @Title: getSystemCurrentTimeAsName 
	 * @Description: 
	 * @return 
	 * @return String 
	 * @author kakasun 
	 * @date 2017年3月7日下午3:29:17
	 */
	public static synchronized String getSystemCurrentTimeAsName(){
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			logger.equals(ExceptionUtils.eMessage(e));
		}
		return Long.toString(System.currentTimeMillis());
	}
}
 