package com.china.rsutil.util.html;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class HtmlTextUtils implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 根据html的url路径取得该页面的文本字符串
	 * 
	 * @param url   html页面的url路径
	 * @return 返回脚本的字符串
	 * @throws MalformedURLException 
	 * @throws IOException
	 */
	public static String readHtmlText(String url) throws MalformedURLException,IOException {
		StringBuffer bf = new StringBuffer();
		try {
			URI u = new URI(url);
			URLConnection urlConnection = u.toURL().openConnection();
			urlConnection.connect();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					urlConnection.getInputStream(), "UTF-8"));
			String lines;
			while ((lines = reader.readLine()) != null) {
				bf.append(lines);
			}

		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return bf.toString();
	}
	/**
	 * 去掉html的标签的到纯文本
	 * @param html
	 * @return 字符串
	 */
	public static String htmlToText(String html){
		Document htmlDoc = Jsoup.parse(html);
		// 得到没有标签文章内容文本
		return htmlDoc.text().trim();
	}
}
