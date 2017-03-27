package com.china.rsutil.util.txt;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
/**
 * txt纯文本操作工具
 * 剪切文本，清除文本空格（包括中文空格），清除换行符，清除标点符号
 * 碎片化文本。
 * @author kakasun
 *
 */
public class TextUtils implements Serializable {

	private static final long serialVersionUID = 1L;
	/**
	 * 将字符串拆分成1000字左右以句号结尾的字符串集合
	 * @param content
	 * @return 拆分后字符串的集合
	 */
	public static List<String> cutContent(String con){
		List<String> textList = new ArrayList<String>();
		while(con.length()>1000){
			String frontText=con.substring(0, 1000);
			String end=con.substring(1000, con.length());
			if(end.indexOf("。")==-1){
				frontText += end;
				con="";
			}else{
				frontText += end.substring(0, end.indexOf("。")+1);
				end = end.substring(end.indexOf("。")+1,end.length());
				con=end;
			}
			textList.add(frontText.trim());
		}
		con = con.trim();
		/*
		 * 遇到了trim不掉的空格
		 */
		if(con.length()>0 && !con.matches("[　 ]+")){
			textList.add(con);
		}
		return textList;
	}
	
	/**
	 * 去除字符串中的空格
	 * @param content 
	 * @return 去除空格后的字符串
	 */
	public static String deleteSpace(String content){
		content = content.replaceAll("[\\s\t　]+", "");
		return content;
	}
	
	/**
	 * 去除字符串中的段落符
	 * @param content
	 * @return 去除段落符后的字符串
	 */
	public static String deleteParagraphCharacter(String content){
		content = content.replaceAll("[\r\n]+", "");
		return content;
	}
	
	/**
	 * 去除字符串中的所有标点符号
	 * @param content
	 * @return 去除标点后的字符串
	 */
	public static String deletePunctuation(String content){
		content = content.replaceAll("[\\pP\\pS]+", "");
		return content;
	}
	
	/**
	 * 将文本进行碎片化，并控制碎片字符的长度，保证每个碎片在语法上词语的完整性
	 * @param text 待碎片化的文本
	 * @param num 碎片化控制的每个碎片字符个数，如果小于或等于0，则 使用默认值12
	 * @return 碎片的list集合
	 * @throws IOException 
	 */
	public static List<String> fragmentText(String text,int num) throws IOException{
		String[] txts = text.split("[\r\n]+");//将文本按照段落进行split
		List<String> list = new ArrayList<String>();
		for(String txt : txts){
			//对文本进行保留标点的分词
			list.addAll(TokensUtils.analyzerEnglishAndChWords(txt));
		}
		if(num <= 0){
			num = 12;//要控制的每个碎片的字符数			
		}
		List<String> strList = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		int len = 0;
		for(String str : list){
			sb.append(str);
			len = sb.length();
			/*
			 * 采用分词的方式，在控制句子长度时保证词语的完整性，不会被拆分到上下两行
			 * 分词时例如“1981年”会被分成“1981”，“年”。若“1981”和“年”刚好被
			 * 分到上下两行，那么，1981会被语音合成为一千九百八十一。
			 * 加上判断，不使整数作为结尾。
			 * 不以小数结尾是防止小数由于小数点被拆分到两行。
			 */
			if(len < num || str.matches("[\\d\\.]+")){
				continue;
			}
			strList.add(sb.toString().trim());
			sb.delete(0, len);
		}
		if(sb.length() > 0){
			strList.add(sb.toString().trim());
		}
		return strList;
	}
	
//	public static void main(String[] args) throws IOException {
//		String path = "K:/123.txt";
//		String str = FileUtils.readFileToString(path,"GBK");
//		List<String> list = fragmentText(str,0);
//		for(String s:list){
//			System.out.println(s);
//		}
//	}
}
