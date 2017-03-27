package com.china.rsutil.util.txt;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import com.chenlb.mmseg4j.ComplexSeg;
import com.chenlb.mmseg4j.Dictionary;
import com.chenlb.mmseg4j.MMSeg;
import com.chenlb.mmseg4j.Word;
/**
 * 对文章进行分词的工具类
 * 使用WhitespaceAnalyzer分词器进行英文分词。
 * mmseg4g分词器,负责中文分词
 * @author kakasun
 *
 */
public class TokensUtils implements Serializable {
	private final static String chRegex = ".*[\u4E00-\u9FA5]+.*";//含有中文的字符串的正则
	private static ComplexSeg cSeg;//mmseg4g分词器,负责中文分词
	private static Analyzer analyzer;//lucene分词器，负责英文分词
	private static Logger logger = Logger.getLogger(TokensUtils.class);
	private static final long serialVersionUID = 1L;
	
	static{
		//实例化mmseg4j分词器的词典
		Dictionary dic = Dictionary.getInstance();
		//实例化mmseg4j分词器,两种分词算法,都是基于正向最大匹配
		//Complex加了四个规则过虑
//		cSeg = new SimpleSeg(dic); 
		cSeg = new ComplexSeg(dic);
		//实例化lucene空格分词器,不会做大小写转换
		analyzer = new WhitespaceAnalyzer();
	}
	/**
	 * 对中英混合进行分词
	 * @param str
	 * @return
	 * @throws IOException
	 */
	public static List<String> analyzerEnglishAndChWords(String str) throws IOException{
		List<String> list = new ArrayList<String>(20);
		List<String> enList = analyzerEnglishWords(str);
		for(String s:enList){
			if(s.matches(chRegex)){
				list.addAll(anaylyzerChWords(s));
			}else{
				list.add(s);
			}
		}
		return list;
	}
	
	/**
	 * 对英文进行分词,保留一切标点
	 * @param str
	 * @return
	 * @throws IOException
	 */
	public static List<String> analyzerEnglishWords(String str) throws IOException{
		str = str.trim();
		//定义一个存放存词的列表  
	    List<String> list=new ArrayList<String>(20);
	    //使用分词器处理测试字符串  
        StringReader reader = new StringReader(str);  
        TokenStream  tokenStream  = analyzer.tokenStream("myfield", reader);  
        tokenStream.reset();  
        CharTermAttribute  term = tokenStream.getAttribute(CharTermAttribute.class); 
		String before = "";
		String curr = "";
		StringBuilder sb = new StringBuilder();
        try{
        	while(tokenStream.incrementToken()){
        		before = curr;
        		String cs = term.toString();
        		curr = sb.append(cs).toString();
        		//添加分词丢失的标点
    			curr = addPunctuation(str, list, before, curr, sb, cs);
        	}
        }finally{
        	tokenStream.close();
        	reader.close();
        }
    	int l = list.size()-1;
		int currL = curr.length();
		int sL = str.length();
		if(sL>currL && l > -1){
			list.set(l,list.get(l)+str.substring(curr.length()));//添加句末标点
		}
	    return list;
	}
	
	/**
	 * 对中文进行分词,保留一切标点
	 * @param str
	 * @return
	 * @throws IOException
	 */
	public static List<String> anaylyzerChWords (String str) throws IOException{  
	    str = str.trim();
	    //去掉除中文和数字的所有字符，添加空格，保持中文的断句
	    String chStr = str.replaceAll("[^0-9\u4E00-\u9FA5]+", " ").trim();
	    char strI = chStr.charAt(0);//取得第一个字符
	    int ind = str.indexOf(strI);//得到第一个字符在没 去掉英文的字符串的位置
	    String before = "";
	    String curr = "";
	    StringBuilder sb = new StringBuilder();
	    List<String> list=new ArrayList<String>(20);
	    if(ind > 0){//根据第一个非英文字符的位置，判断是否在字符串头部有被删掉的英文数字
	    	String head = str.substring(0, ind);//获取这个非数字中文字符串
	    	sb.append(head);
	    	list.add(head);//将这个字符串添加到分词结果集合中
	    }
		//定义一个存放存词的列表  
	    MMSeg mmSeg = new MMSeg(new StringReader(chStr), cSeg);
		Word word = null;
		// 迭代获取分词结果
		while ((word = mmSeg.next()) != null) {
			before = curr;
			String cs = word.getString();
			curr = sb.append(cs).toString();
			//添加分词丢失的标点
			curr = addPunctuation(str, list, before, curr, sb, cs);
		}
		int l = list.size()-1;
		int currL = curr.length();
		int sL = str.length();
		if(sL>currL && l > -1){
			list.set(l,list.get(l)+str.substring(curr.length()));//添加句末标点
		}
		return list; 
	}
	/**
	 * 和原句对比，添加回分词丢失的标点
	 * @param str
	 * @param list
	 * @param before
	 * @param curr
	 * @param sb
	 * @param cs
	 * @return
	 */
	private static String addPunctuation(String str, List<String> list,
			String before, String curr, StringBuilder sb, String cs) {
		boolean isSame = true;
		try{
			isSame = curr.equals(str.substring(0, curr.length()));
		}catch(Exception e){
			logger.info("<------------------------------------------------>");
			logger.info("分词isSame错误：");
			logger.info("str:" + str);
			logger.info("curr:" + curr);
			logger.info("before:" + before);
			logger.info("cs:" + cs);
			logger.info("<------------------------------------------------>");
		}
		// 如果有标点,加入标点
		if (!isSame) {
			// 计算标点个数
			int start = before.length();
			int end = str.indexOf(cs, start);
			int listSize = list.size()-1;
			String punctuation = " ";
			if (end > start) {//有标点
				punctuation = str.substring(start, end);
				int pLength = punctuation.length();
				//是否是成对的标点符号
				boolean twin = false;
				//单个标点
				if(pLength == 1){
					twin = punctuation.matches("[('\"<（‘“《]");//判断是否是成对标点符号
					if(twin){
						//是成对标点则加到词的头部
						cs = punctuation + cs;
						punctuation = "";
					}else{
						//不是成对的则加到上一个词的尾部
						if(listSize>-1){
							list.set(listSize, list.get(listSize)
									+ punctuation);
						}else{
							list.add(punctuation);
						}												
					}
				}else if(pLength > 1){//多个标点
					int index = punctuation.length()-1;
					//分成最后一个标点和其余标点
					String punctuation2 = punctuation.charAt(index)+"";
					String punctuation1 = punctuation.substring(0, index);
					//最后一个标点是否为成对标点
					twin = punctuation2.matches("[('\"<（‘“《]");//判断是否是成对标点符号
					if(twin){
						//将最后一个标点加到词的头部
						cs = punctuation2 + cs;
						//将其余标点加到上一个词的尾部
						if(listSize>-1){
							list.set(listSize, list.get(listSize)
									+ punctuation1);
						}else{
							list.add(punctuation1);
						}
						punctuation = punctuation1;
					}else{//不是成对标点则将所有标点加到上一个词的尾部
						if(listSize>-1){
							list.set(listSize, list.get(listSize)
									+ punctuation);
						}else{
							list.add(punctuation);
						}
					}
				}
				before = before + punctuation;
			} else if (end < start) {
				/*
				 * 测试中用于记录，补添分词去掉的标点符号的过程有错误的语句
				 * 测试结束正式使用时，应当注解掉
				 */
				logger.info("<=======================================================>");
				logger.info("分词回添标点错误：");
				logger.info("end-start:" + (end - start));
				logger.info("str:" + str);
				logger.info("curr:" + curr);
				logger.info("before:" + before);
				logger.info("cs:" + cs);
				logger.info("<=======================================================>");
				
			}
			sb.delete(0, sb.length());
			sb.append(before);
			sb.append(cs);
			curr = sb.toString();
		}
		list.add(cs);
		return curr;
	}
	
	public static void main(String[] args) {
		String s = "“2004年以前，重金属项目的审批并没有被单独列出来，只是按照常规投资项目审批的程序走，也就是按照投资规模确定审批级别。”中国电池工业协会浙江铅蓄电池行业协会秘书长姚令春在接受《中国经济周刊》采访时介绍说。 ";
		try {
			List<String> list = TokensUtils.anaylyzerChWords(s);
			System.out.println("\n\n\n");
			for(String str:list){
				System.out.println(str);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
