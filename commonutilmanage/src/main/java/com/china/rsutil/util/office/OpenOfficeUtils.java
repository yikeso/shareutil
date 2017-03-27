package com.china.rsutil.util.office;  

import java.io.File;
import java.net.ConnectException;

import org.apache.log4j.Logger;

import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.OpenOfficeDocumentConverter;
import com.china.rsutil.util.exception.ExceptionUtils;

/** 
 * 使用openoffice对office文档进行操作
 * 此方法必须在本机安装openOffice并启动服务
 *@Title OpenOfficeUtils.java 
 *@description TODO 
 *@time 2017年3月8日 下午12:12:28 
 *@author kakasun 
 **/
public class OpenOfficeUtils {
	//openOffice服务连接器
	private static final OpenOfficeConnection CONNECT = new SocketOpenOfficeConnection(8100);
	
	private static Logger logger = Logger.getLogger(OpenOfficeUtils.class);
	/**
	 * office转pdf
	 * 此方法必须在本机安装openOffice并启动服务
	 * @Title: officeToPdf 
	 * @Description: 
	 * @param officePath
	 * @param pdfPath 
	 * @return 
	 * @author kakasun 
	 * @date 2017年3月8日下午12:15:11
	 */
	public static boolean officeToPdf(String officePath,String pdfPath){
		return officeToPdf(new File(officePath),new File(pdfPath));
	}
	
	/**
	 * office转pdf
	 * 此方法必须在本机安装openOffice并启动服务
	 * @Title: officeToPdf 
	 * @Description: 
	 * @param office
	 * @param pdf 
	 * @return 
	 * @author kakasun 
	 * @date 2017年3月8日下午12:15:36
	 */
	public static boolean officeToPdf(File office,File pdf){
		if(!office.exists()){
			throw new RuntimeException("office 文件不存在，无法转换pdf");
		}
		File dir = pdf.getParentFile();
		//pdf文件所在文件夹不存在则创建
		if(!dir.exists()){
			dir.mkdirs();
		}
		try {
			CONNECT.connect();
		} catch (ConnectException e) {
			logger.error(ExceptionUtils.eMessage(e));
			return false;
		}
        DocumentConverter converter=new OpenOfficeDocumentConverter(CONNECT);             
        //office转pdf
        converter.convert(office, pdf);
        //关闭连接
        CONNECT.disconnect();
        return true;
	}
	
}
 