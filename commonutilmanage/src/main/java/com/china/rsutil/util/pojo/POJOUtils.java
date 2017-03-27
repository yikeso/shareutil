package com.china.rsutil.util.pojo;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.SqlDateConverter;
import org.apache.commons.beanutils.converters.SqlTimestampConverter;

/**
 * java bean实体类的相关工具
 *
 * @Title POJOUtils.java
 * @description TODO
 * @time 2017年3月8日 下午4:19:46
 * @author kakasun
 **/
public class POJOUtils {

	static {
		// 注册日期的转换器，即允许BeanUtils.copyProperties()时对日期进行格式转换
		//sql.Date
		ConvertUtils.register(new SqlDateConverter(null), java.sql.Date.class);
		//util.Date
		ConvertUtils.register(new SqlDateConverter(null), java.util.Date.class);
		//sql.Timestamp
		ConvertUtils.register(new SqlTimestampConverter(null),
				java.sql.Timestamp.class);
	}
	
	/**
	 * 复制两个实体类中的同名属性
	 * @Title: copyProperties 
	 * @Description: 
	 * @param target 目标实体类
	 * @param source 源实体类
	 * @return void 
	 * @author kakasun 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @date 2017年3月8日下午4:24:50
	 */
	public static void copyProperties(Object target,Object source) throws IllegalAccessException, InvocationTargetException{
		// 支持对日期copy
		BeanUtils.copyProperties(target, source);
	}
}
