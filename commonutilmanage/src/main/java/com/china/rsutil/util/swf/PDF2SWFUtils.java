package com.china.rsutil.util.swf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.china.rsutil.po.CommandExecute;
import com.sun.istack.internal.logging.Logger;

/**
 * @Title PDF2SWFUtils.java
 * @description TODO
 * @time 2017年3月8日 下午1:27:14
 * @author kakasun
 **/
public class PDF2SWFUtils {

	private static Logger logger = Logger.getLogger(PDF2SWFUtils.class);
	/**
	 * PDF转swf
	 * @Title: PDF2SWF 
	 * @Description: 
	 * @param pdfPath pdf文件路径
	 * @param swfPath swf文件路径
	 * @param toolPath pdf转swf工具路径
	 * @param fontPath
	 * @return 
	 * @return int 错误代码  0表示转换成功
	 * @author kakasun 
	 * @date 2017年3月8日下午1:29:44
	 */
	public static int PDF2SWF(String pdfPath, String swfPath,String toolPath, String fontPath) {	
		return PDF2SWF(new File(pdfPath),new File(swfPath),toolPath,fontPath);
	}
	
	/**
	 * PDF转swf
	 * @Title: PDF2SWF 
	 * @Description: 
	 * @param pdfPath pdf文件
	 * @param swfPath swf文件
	 * @param toolPath pdf转swf工具路径
	 * @param fontPath
	 * @return 
	 * @return int 错误代码  0表示转换成功
	 * @author kakasun 
	 * @date 2017年3月8日下午1:29:44
	 */
	public static int PDF2SWF(File pdf, File swf,String toolPath, String fontPath) {
		if(!pdf.exists()){
			throw new RuntimeException("pdf文件不存在，无法进行pdf转swf");
		}
		File dir = swf.getParentFile();
		if(!dir.exists()){
			dir.mkdirs();
		}
		// 因为下面进行系统调用，这样就会把系统执行的操作新开启一个线程（在此linux也叫进程），
		// 所以它和主扫描程序是独立运行，所以下次还会扫描这个转换中的文件，所以这里要将它设置为不可读，
		pdf.setReadable(false);
		List<String> command = new ArrayList<String>();
		command.add(toolPath);// 从配置文件里读取
		command.add(pdf.getAbsolutePath());
		command.add("-o");
		command.add(swf.getAbsolutePath());
		long filesize = pdf.length();
		if(filesize > 0 && filesize < 15728640){
			command.add("-s");
			command.add("poly2bitmap");// 加入poly2bitmap的目的是为了防止出现大文件或图形过多的文件转换时的出错，没有生成swf文件的异常
		}
		command.add("-T");
		command.add("9");
		command.add("-f");
		command.add("-s");
		command.add("languagedir="+fontPath);
		CommandExecute ce = new CommandExecute(command);
		ce.run();
		return ce.getResultCode();
	}
	
}
