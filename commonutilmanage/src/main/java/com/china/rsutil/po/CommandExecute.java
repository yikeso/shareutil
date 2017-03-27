package com.china.rsutil.po;  

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.log4j.Logger;

import com.china.rsutil.util.exception.ExceptionUtils;

/** 
 * 执行系统命令
 *@Title CommandExecute.java 
 *@description TODO 
 *@time 2017年3月7日 下午1:25:21 
 *@author kakasun 
 **/
public class CommandExecute implements Runnable {

	String command;
	String resultMessage;//linux系统命令执行的结果
	String errorMessage;//linux系统命令执行过程中的错误信息
	int resultCode;//0表示运行正常
	private static Logger logger = Logger.getLogger(CommandExecute.class);
	/**
	 * @param command linux系统命令
	 */
	public CommandExecute(String command){
		this.command = command;
	}
	/**
	 * 传入list命令集合
	 * @Title: setCommand 
	 * @Description: 
	 * @param commandList 
	 * @return void 
	 * @author kakasun 
	 * @date 2017年3月8日下午1:21:45
	 */
	public CommandExecute(List<String> commandList){
		if(commandList == null || commandList.size() == 0){
			throw new RuntimeException("传入命令为空");
		}
		StringBuffer sb = new StringBuffer();
		for(String s : commandList){
			sb.append(s);
			sb.append(" ");
		}
		this.command = sb.toString().trim();
	}
	/**
	 * 执行linux系统命令
	 */
	@Override
	public void run() {
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(command);
			InputStream errorStream = process.getErrorStream();
			//另起一个线程接收错误信息,否则会造成标准输出流的阻塞
			ErrorStreamRunable esr = new ErrorStreamRunable(errorStream,this);//内部类
			Thread t = new Thread(esr);
			t.start();
			InputStream inputStream = process.getInputStream();
			InputStreamRunable isr = new InputStreamRunable(inputStream,this);
			Thread t2 = new Thread(isr);
			t2.start();
			resultCode = process.waitFor();	
		} catch (InterruptedException e) {
			logger.error(ExceptionUtils.eMessage(e));
			resultCode = 1;
		} catch (IOException e) {
			logger.error(ExceptionUtils.eMessage(e));
			resultCode = 1;
		}finally{
			if(process != null){
				process.destroy();
			}
		}
	}
	
	public String getResultMessage() {
		return resultMessage;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public int getResultCode() {
		return resultCode;
	}

	
	/**
	 * 内部类，用来接收运行系统命令时输出结果信息
	 * @Title CommandExecute.java
	 * @description TODO 
	 * @time 2017年3月7日 下午1:32:14
	 * @author kakasun
	 */
	class InputStreamRunable implements Runnable{
		
		InputStream inputStream;
		CommandExecute ce;
		/**
		 * 
		 * @param errorStream 错误信息输入流
		 * @param errorMessage 错误信息
		 */
		public InputStreamRunable(InputStream inputStream,CommandExecute ce){
			this.inputStream = inputStream;
			this.ce = ce;
		}
		
		/**
		 * 接收执行系统命令的结果信息
		 */
		@Override
		public void run() {
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			StringBuffer sb = new StringBuffer(2000);
			String line = null;
			try {
				while((line = br.readLine()) != null){
					sb.append(line);
				}
				ce.resultMessage = sb.toString();
			} catch (IOException e) {
				logger.error(ExceptionUtils.eMessage(e));
			}
		}
		
	}
	
	/**
	 * 内部类，用来接收运行系统命令是产生的错误信息
	 * @Title CommandExecute.java
	 * @description TODO 
	 * @time 2017年3月7日 下午1:32:14
	 * @author kakasun
	 */
	class ErrorStreamRunable implements Runnable{
		
		InputStream errorStream;
		CommandExecute ce;
		/**
		 * 
		 * @param errorStream 错误信息输入流
		 * @param errorMessage 错误信息
		 */
		public ErrorStreamRunable(InputStream errorStream,CommandExecute ce){
			this.errorStream = errorStream;
			this.ce = ce;
		}
		
		/**
		 * 接收执行系统命令的错误信息
		 */
		@Override
		public void run() {
			BufferedReader br = new BufferedReader(new InputStreamReader(errorStream));
			StringBuffer sb = new StringBuffer(1000);
			String line = null;
			try {
				while((line = br.readLine()) != null){
					sb.append(line);
				}
				ce.errorMessage = sb.toString();
			} catch (IOException e) {
				logger.error(ExceptionUtils.eMessage(e));
			}
		}
		
	}

}
 