package com.china.rsutil.util.date;  

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/** 
 * 日期相关操作工具
 *@Title DateUtil.java 
 *@description TODO 
 *@time 2017年3月7日 下午2:07:20 
 *@author kakasun 
 **/
public class DateUtils {

	private static final SimpleDateFormat YMD = new SimpleDateFormat("yyyy/M/d");
	/**
	 * 得到当前年份
	 * @return
	 */
	public static String getYear(){
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		return String.valueOf(year);
	}
	/**
	 * 得到当前月份
	 * @return
	 */
	public static String getMonday(){
		Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONDAY)+1;
		return String.valueOf(month);
	}
	/**
	 * 得到当前日
	 * @return
	 */
	public static String getDate(){
		Calendar cal = Calendar.getInstance();
		int day = cal.get(Calendar.DATE);
		return String.valueOf(day);
	}
	/**
	 * 得到   年/月/日   字符串
	 * @Title: getYMD 
	 * @Description: 
	 * @return 
	 * @return String 
	 * @author kakasun 
	 * @date 2017年3月7日下午2:11:44
	 */
	public static String getCurrentYMD(){
		Date d = new Date();
		return YMD.format(d);
	}
	
//	public static void main(String[] args) {
//		String date = "2016/12/24";
//		try {
//			Date d = YMD.parse(date);
//			System.out.println(YMD.format(d));
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println(getCurrentYMD());
//	}
}
 