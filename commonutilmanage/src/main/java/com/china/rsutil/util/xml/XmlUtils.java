package com.china.rsutil.util.xml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * @Title XmlUtils.java
 * @description TODO
 * @time 2017年3月9日 上午11:13:31
 * @author kakasun
 **/
public class XmlUtils {

	/**
	 * 将xml格式化为字符串
	 * 
	 * @Title: getXmlString
	 * @Description:
	 * @param document
	 * @return
	 * @throws IOException
	 * @return String
	 * @author kakasun
	 * @date 2017年3月9日上午11:15:35
	 */
	public static String getXmlString(Document document) throws IOException {
		// 格式化输出xml文件字符串
		Format format = Format.getCompactFormat();
		format.setEncoding("utf-8");
		// 这行保证输出后的xml的格式
		format.setIndent("\t");
		XMLOutputter xmlout = new XMLOutputter(format);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			xmlout.output(document, baos);
			String str = baos.toString("utf-8");
			return str;
		} finally {
			baos.close();
		}
	}

	/**
	 * 将xml写入文件
	 * @Title: writerXmlToFile 
	 * @Description: 
	 * @param document xml对象
	 * @param xmlPath xml文件路径
	 * @return void 
	 * @author kakasun 
	 * @throws IOException 输出流操作
	 * @date 2017年3月9日上午11:17:49
	 */
	public static void writerXmlToFile(Document document, String xmlPath) throws IOException {
		writerXmlToFile(document, new File(xmlPath));
	}

	/**
	 * 将xml写入文件
	 * @Title: writerXmlToFile 
	 * @Description: 
	 * @param document xml对象
	 * @param xml xml文件
	 * @return void 
	 * @author kakasun 
	 * @throws IOException 输出流操作
	 * @date 2017年3月9日上午11:18:21
	 */
	public static void writerXmlToFile(Document document, File xml) throws IOException {
		// 格式化输出xml文件字符串
				Format format = Format.getCompactFormat();
				format.setEncoding("utf-8");
				// 这行保证输出后的xml的格式
				format.setIndent("\t");
				XMLOutputter xmlout = new XMLOutputter(format);
				File dir = xml.getParentFile();
				if(!dir.exists()){
					dir.mkdirs();
				}
				FileOutputStream fos = new FileOutputStream(xml);
				try {
					xmlout.output(document, fos);
				} finally {
					fos.close();
				}
	}
}
