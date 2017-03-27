package com.china.rsutil.util.file;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.util.Deque;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.DelayQueue;

import org.apache.log4j.Logger;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;

import com.china.rsutil.po.CommandExecute;

/**
 * @author kakasun
 * 
 */
public class FileUtils {
	
	//缓冲区大小
	private final static int BUF_SIZE = 1024*100;
	private static Logger logger = Logger.getLogger(FileUtils.class);
	public final static DelayQueue<DeleteFileTask> dQue = new DelayQueue<DeleteFileTask>();

	static{
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true){
					DeleteFileTask dft = dQue.poll();
					if(dft != null){						
						dft.run();
//						System.out.println(System.currentTimeMillis());
					}
				}
			}
		}).start();
	}

	/**
	 * 对文件进行校验
	 * 校验传入的file是否是文件，不是返回false；
	 * 文件名是否乱码，是返回false；
	 * 文件是否正在被操作，是返回false
	 * @Title: validate 
	 * @Description: 
	 * @param file file对象
	 * @return 
	 * @return boolean 
	 * @author kakasun 
	 * @date 2017年3月10日上午9:15:41
	 */
	public static boolean validate(File file){
		if(!file.exists()){
			logger.info(file.getAbsolutePath() + " 不存在");
			return false;
		}
		//文件夹
		if(file.isDirectory()){
			logger.info(file.getAbsolutePath() + " 是文件夹");
			return false;
		}
		//文件名乱码
		if(FileUtils.checkFileNameMessyCode(file)){
			return false;
		}
		//文件正在被使用
		if(FileUtils.isUsed(file)){
			logger.info(file.getAbsolutePath() + " 正在使用中。");
			return false;
		}
		return true;
	}
	
	/**
	 * 检查获取文件 名是否乱码
	 * @Title: checkFileName 
	 * @Description: 
	 * @param file 文件
	 * @return 
	 * @return boolean true是乱码，false不是乱码
	 * @author kakasun 
	 * @date 2017年3月7日下午1:18:42
	 */
	public static boolean checkFileNameMessyCode(File file){
		FileInputStream fis = null;
		try{
			fis = new FileInputStream(file.getPath());
		}catch(IOException e){
			e.printStackTrace();
			return true;
		}finally{
			if(fis != null){
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}
	
	/**
	 * 是用linux lsof命令，检查文件是否被ftp等其它进程占用
	 * @Title: isUsed 
	 * @Description: 
	 * @param file
	 * @return 
	 * @return boolean 
	 * @author kakasun 
	 * @date 2017年3月7日下午1:20:55
	 */
	public static boolean isUsed(File file) {
		// lsof命令查看打开该文件的进程
		String command = "/usr/sbin/lsof " + file.getAbsolutePath();
		CommandExecute ce = new CommandExecute(command);
		ce.run();
		String result = ce.getResultMessage();
		if (result.indexOf("sftp-serv") != -1 || result.indexOf("vsftpd") != -1) {
			logger.info("文件正在被操作");
			return true;
		} 
		if(file.renameTo(file)){
			return false;
		}else{
			return true;
		}
	}
	
	/**
	 * 将传入的数据流中的数据写入文件
	 * @param path 写入文件路径
	 * @param sourceStream 输入流
	 * @author kakasun
	 * @throws IOException 
	 */
	public static void writeFile(String path, InputStream sourceStream) throws IOException {
		if(sourceStream == null){
			throw new RuntimeException("传入的 输入流 为空");
		}
		File file = new File(path);
		creatFileIfNotExists(file);
		OutputStream bos = new FileOutputStream(file);
		try {
			int length = -1;
			byte[] buffer = new byte[BUF_SIZE];
			while ((length = sourceStream.read(buffer)) != -1) {
				bos.write(buffer, 0, length);
			}
		} finally {
			sourceStream.close();
			bos.close();
		}
	}

	/**
	 * 将一个文件覆盖写入另一个文件
	 * @Title: overWriterFile 
	 * @Description: 
	 * @param sourceFile
	 * @param targetFile
	 * @throws IOException 
	 * @return void 
	 * @author kakasun 
	 * @date 2017年3月7日下午5:03:46
	 */
	public static void overWriterFile(String sourcePath, String targetPath) throws IOException{
		overWriterFile(new File(sourcePath), new File(targetPath));
	}
	
	/**
	 * 将一个文件覆盖写入另一个文件
	 * @Title: overWriterFile 
	 * @Description: 
	 * @param sourceFile
	 * @param targetFile
	 * @throws IOException 
	 * @return void 
	 * @author kakasun 
	 * @date 2017年3月7日下午5:03:46
	 */
	public static void overWriterFile(File sourceFile, File targetFile) throws IOException{
		File targetDir = targetFile.getParentFile();
		if(!targetDir.exists()){
			targetDir.mkdirs();
		}
		FileInputStream fis = null;
		FileOutputStream fos = null;
		byte[] buffer = new byte[BUF_SIZE];
		int l = -1;
		try{
			fis = new FileInputStream(sourceFile);
			fos = new FileOutputStream(targetFile);
			while((l = fis.read(buffer)) != -1){
				fos.write(buffer, 0, l);
			}
		}finally{
			if(fis != null){
				fis.close();
			}
			if(fos != null){
				fos.close();
			}
		}
	}
	/**
	 * 移动文件
	 * @Title: moveFile 
	 * @Description: 
	 * @param source 移动源文件
	 * @param target 目标路径（文件路径）
	 * @return 
	 * @return boolean 
	 * @author kakasun 
	 * @throws FileNotFoundException 
	 * @date 2017年3月10日下午2:16:54
	 */
	public static boolean moveFile(String sourcePath,String targetPath) throws FileNotFoundException{
		return moveFile(new File(sourcePath),new File(targetPath));
	}
	/**
	 * 移动文件
	 * @Title: moveFile 
	 * @Description: 
	 * @param source 移动源文件
	 * @param target 目标路径(文件路径)
	 * @return 
	 * @return boolean 
	 * @author kakasun 
	 * @throws FileNotFoundException 
	 * @date 2017年3月10日下午2:15:24
	 */
	public static boolean moveFile(File source,File target) throws FileNotFoundException{
		if(!source.exists()){
			throw new FileNotFoundException("待移动文件 "+source.getAbsolutePath()+" 不存在.");
		}
		File dir = target.getParentFile();
		if(!dir.exists()){
			dir.mkdirs();
		}
		return source.renameTo(target);
	}
	/**
	 * 将传入的file文件复制到新的目录
	 * @param sourceFile 待复制的源文件
	 * @param targetDir 目标目录
	 * @param filename 新文件名(带文件后缀名)
	 * @return 复制后的file对象
	 * @throws IOException
	 */
	public static File copyFile(File sourceFile, String targetDir, String filename) throws IOException {
		if(!sourceFile.isFile()){
			throw new RuntimeException("传入的待复制file的不是文件");
		}
		targetDir = formatFilePath(targetDir);
		File targetDirFile = new File(targetDir);
		File targetFile = null;
		if(targetDirFile.isDirectory()){
			if(!targetDir.endsWith("/")){
				targetDir = targetDir + "/";
			}
			targetFile = new File(targetDir + filename);
		}else{
			String targetPath = targetDir.substring(0, targetDir.lastIndexOf("/")+1)
							  + filename;
			targetFile = new File(targetPath);
		}
		//复制文件
		copyFile(sourceFile, targetFile);
		return targetFile;
	}
	

	/**
	 * 复制传入的文件到传入的目标路径
	 * 只能复制文件,目标路径如果是文件，则复制到文件所在目录
	 * 目标路径如果是文件夹，文件夹若存在，复制到文件夹里，
	 * 如果文件夹不存在，则复制到问价夹所在路径
	 * @param sourceFile 待复制源文件
	 * @param destFile 目标文件路径
	 * @throws IOException 
	 * 
	 * */
	public static void copyFile(File sourceFile, File destFile) throws IOException {
		if(!sourceFile.exists()){
			throw new RuntimeException("源文件不存在，复制文件失败");
		}
		if(!sourceFile.isFile()){
			throw new RuntimeException(sourceFile.getAbsolutePath()+"不是文件，复制文件失败");
		}
		
		if (destFile.exists() && destFile.isDirectory()) {
			String destPath = destFile.getAbsolutePath();
			formatFilePath(destPath);
			if (!destPath.endsWith("/")) {
				destPath = destPath + "/";
			}
			destFile = new File(destPath + sourceFile.getName());
		} else {
			File parent = destFile.getParentFile();
			String parentPath = parent.getAbsolutePath();
			formatFilePath(parentPath);
			if (!parentPath.endsWith("/")) {
				parentPath = parentPath + "/";
			}
			destFile = new File(parentPath + sourceFile.getName());
		}
		logger.debug("正在复制"+sourceFile.getAbsolutePath()+" 到   "+destFile.getAbsolutePath());
		creatFileIfNotExists(destFile);
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			fis = new FileInputStream(sourceFile);
			fos = new FileOutputStream(destFile);
			if (!destFile.getParentFile().exists()) {
				destFile.getParentFile().mkdirs();
			}
			byte[] buffer = new byte[BUF_SIZE];
			int count = -1;
			while ((count = fis.read(buffer)) > 0) {
				fos.write(buffer, 0, count);
			}
		} finally {
			if (fis != null) {
				fis.close();
			}
			if (fos != null) {
				fos.close();
			}
		}
	}
	
	/**
	 * 复制文件
	 * @param sourcePath 源文件路径
	 * @param targetPath 目标文件路径
	 * @throws IOException
	 */
	public static void copyFile(String sourcePath, String targetPath) throws IOException {
		sourcePath = formatFilePath(sourcePath);
		targetPath = formatFilePath(targetPath);
		copyFile(new File(sourcePath), new File(targetPath));
	}

	/**
	 * 复制文件夹
	 * @param sourceDir 待复制文件夹
	 * @param targetDir 目标文件夹
	 * @author kakasun
	 * @throws IOException
	 */
	public static void copyDirectiory(String sourceDir, String targetDir)
			throws IOException {
		sourceDir = formatFilePath(sourceDir);
		targetDir = formatFilePath(targetDir);
		copyDirectiory(new File(sourceDir), new File(targetDir));
	}
	
	/**
	 * 复制文件夹
	 * @param sourceDir
	 * @param targetDir
	 * @author kakasun
	 * @throws IOException
	 */
	public static void copyDirectiory(File sourceDir, File targetDir)
			throws IOException {
		if(!sourceDir.exists()){
			throw new RuntimeException("待复制文件夹不存在");
		}
		//获取这个文件或文件夹的名称
		//源file为文件
		if(sourceDir.isFile()){
			//目标为文件直接复制
			copyFile(sourceDir, targetDir);
		}else{
			String sourceDirPath = sourceDir.getAbsolutePath();
			String targetDirPath = targetDir.getAbsolutePath();
			formatFilePath(targetDirPath);
			if(!targetDirPath.endsWith("/")){
				targetDirPath += "/";
			}
			targetDirPath += sourceDir.getName();
			Queue<File> dirQueue = new LinkedList<File>();
			//将文件夹放入队列
			dirQueue.offer(sourceDir);	
			while(dirQueue.size() > 0){//当队列长度大于零
				File f = dirQueue.poll();//取出队列中的第一个file
				//计算目标路径
				String tagetPath = f.getAbsolutePath().replace(sourceDirPath,targetDirPath);
				formatFilePath(tagetPath);
				File targetFile = new File(tagetPath);
				
				//取出的file是文件复制文件
				if(f.isFile()){
					copyFile(f, targetFile);
				}else{
					//取出的file是文件夹，创建文件夹，并将文件夹中的所有
					targetFile.mkdirs();
					//file添加入队列等待复制
					File[] files = f.listFiles();
					for(File file : files){
						dirQueue.offer(file);
					}
				}
			}
		}
	}
	
	/**
	 * 将输入流中读取到的数据写入输出流
	 * 流未关闭
	 * @param input
	 * @param output
	 * @throws IOException
	 */
	public static void inStreamToOutStream(InputStream input, OutputStream output)
			throws IOException {
		byte buf[] = new byte[BUF_SIZE];
		int b = -1;
		while ((b = input.read(buf)) > 0) {
			output.write(buf, 0, b);
		}
		output.flush();
	}

	/**
	 * 将字符串写入文件
	 * @param filePath
	 * @param content
	 * @author kakasun
	 * @throws IOException
	 */
	public static void writeStringToFile(String filePath, String content)
			throws IOException {
		filePath = formatFilePath(filePath);
		File file = new File(filePath);
		writeStringToFile(file, content);
	}
	
	/**
	 * 将字符串写入文件
	 * @param filePath
	 * @param content
	 * @author kakasun
	 * @throws IOException
	 */
	public static void writeStringToFile(File file, String content) throws IOException{
		creatFileIfNotExists(file);
		FileWriter writer =  new FileWriter(file);
		try {
			writer.write(content);
			writer.flush();
		} finally {
			writer.close();
		}
	}
	
	/**
	 * 将字符串按指定字符集写入文件
	 * @param filePath 文件路径
	 * @param fileContent
	 * @param encoding 字符集
	 * @throws IOException 
	 */
	public static void writeStringToFile(String filePath, String fileContent,
			String encoding) throws IOException {
		filePath = formatFilePath(filePath);
		File file = new File(filePath);
		writeStringToFile(file, fileContent,encoding);
	}
	
	/**
	 * 将字符串按指定字符集写入文件
	 * @param filePath 文件路径
	 * @param fileContent
	 * @param encoding 字符集
	 * @throws IOException 
	 */
	public static void writeStringToFile(File file, String fileContent,
			String encoding) throws IOException {
		creatFileIfNotExists(file);
		byte[] contentByte = fileContent.getBytes(encoding);
		FileOutputStream fos = new FileOutputStream(file);
		try{
			fos.write(contentByte);
			fos.flush();
		}finally{
			fos.close();
		}
	}
	
	/**
	 * 判断传入的文件是否存在，不存在则创建
	 * @param file
	 * @author kakasun
	 * @return
	 * @throws IOException 
	 */
	public static void creatFileIfNotExists(File file) throws IOException {
		// file存在返回true
		if (file.exists()) {
			return;
		}
		File parentDirectory = file.getParentFile();
		// 创建所在文件夹
		parentDirectory.mkdirs();
		// 创建文件
		file.createNewFile();
	}
	
	/**
	 * 读取文件，转换为字符串
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	public static String readFileToString(Reader reader) throws IOException {
		if(reader == null){
			throw new RuntimeException("读取文件失败");
		}
		BufferedReader br = new BufferedReader(reader);;
		StringBuilder sb = new StringBuilder(1000);
		try {
			
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append("\r\n");
			}
		} finally {
			br.close();
		}
		return sb.toString();
	}

	/**
	 * 读取文件，转换为字符串
	 * @param reader
	 * @param code 文本编码字符集
	 * @return
	 * @throws IOException
	 */
	public static String readFileToString(InputStream stream,String code) throws IOException {
		return readFileToString(new InputStreamReader(stream,code));
	}

	/**
	 * 读取文件，转换为字符串
	 * @param reader
	 * @param code 文本编码字符集
	 * @return
	 * @throws IOException
	 */
	public static String readFileToString(InputStream stream) throws IOException {
		return readFileToString(new InputStreamReader(stream));
	}
	
	/**
	 * 读取文件，转换为字符串
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	public static String readFileToString(File file) throws IOException {
		if(!file.exists()){
			throw new RuntimeException("传入的file不存在");
		}
		if(!file.isFile()){
			throw new RuntimeException("传入的不是一个文件");
		}
		String codeStr = codeString(file);
		FileInputStream fis = new FileInputStream(file);
		try{
			return readFileToString(fis,codeStr);
		}finally{
			fis.close();
		}
	}
	
	/**
	 * 以指定字符集读取文件，转换为字符串
	 * @param file
	 * @param code 字符集类型
	 * @return
	 * @throws IOException
	 */
	public static String readFileToString(File file,String code) throws IOException {
		if(!file.exists()){
			throw new RuntimeException("传入的file不存在");
		}
		if(!file.isFile()){
			throw new RuntimeException("传入的不是一个文件");
		}
		FileInputStream fis = new FileInputStream(file);
		try{
			return readFileToString(fis,code);
		}finally{
			fis.close();
		}
	}
	
	/**
	 * 读取文件，转换为字符串
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static String readFileToString(String sourceFile) throws IOException {
		sourceFile = formatFilePath(sourceFile);
		return readFileToString(new File(sourceFile));
	}
	
	/**
	 * 以指定字符集读取文件，转换为字符串
	 * @param file
	 * @pramati code
	 * @return
	 * @throws IOException
	 */
	public static String readFileToString(String sourceFile,String code) throws IOException {
		sourceFile = formatFilePath(sourceFile);
		return readFileToString(new File(sourceFile),code);
	}

	/**
	 * 读取文件中的文本信息，按行返回一个字符串数组
	 * @param sourceFile
	 * @return
	 * @throws IOException
	 */
	public static String[] readFileLines(String filePath) throws IOException {
		filePath = formatFilePath(filePath);
		return readFileLines(new File(filePath));
	}
	
	/**
	 * 读取文件中的文本信息，按行返回一个字符串数组
	 * @param sourceFile
	 * @return
	 * @throws IOException
	 */
	public static String[] readFileLines(File file) throws IOException {
		String str = readFileToString(file);
		return str.split("(\r\n)");
	}

	/**
	 * 判断文件的编码格式
	 * 通过获取文件的前三个字节来判断，Unicode编码 前两个字节为FFFE； 
	 * Unicode big endian编码的前两字节为FEFF；UTF-8编码的前两字节为EFBB；
	 * @param filePath
	 * @return 文件编码格式
	 * @throws Exception
	 * @author kakasun
	 */
	public static String codeString(String filePath){
		File file = new File(filePath);
		return codeString(file);
	}
	
	/**
	 * 判断文件的编码格式
	 * 通过获取文件的前三个字节来判断，Unicode编码 前两个字节为FFFE； 
	 * Unicode big endian编码的前两字节为FEFF；UTF-8编码的前两字节为EFBB；
	 * @param filePath
	 * @return 文件编码格式
	 * @throws Exception
	 * @author kakasun
	 */
	public static String codeString(File file) {
		if (file == null || !file.exists()) {
			throw new RuntimeException(file.getAbsolutePath() + "文件不存在...");
		}
		RandomAccessFile raf = null;
		int p = 0xefbb;
		try {
			raf = new RandomAccessFile(file, "r");
			p = (raf.read() << 8) + raf.read();
		} catch (IOException e) {
			return "UTF-8";
		} finally {
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		String code = "UTF-8";
		// 其中的 0xefbb、0xfffe、0xfeff、0x5c75这些都是这个文件的前面两个字节的16进制数
		switch (p) {
		case 0xefbb:
			code = "UTF-8";
			break;
		case 0xfffe:
			code = "Unicode";
			break;
		case 0xfeff:
			code = "UTF-16BE";
			break;
		case 0x5c75:
			code = "ASCII";
			break;
		default:
			code = "GBK";
		}
		return code;
	}
	
	/**
	 * 获取文件大小，-1表示获取文件大小失败
	 * @param filepath
	 * @return
	 */
	public static long getFileSize(String filepath) {
		filepath = formatFilePath(filepath);
		File sourceFile = new File(filepath);
		return getFileSize(sourceFile);
	}

	/**
	 * 获取文件大小，-1表示获取文件大小失败
	 * @param sourceFile
	 * @return
	 */
	public static long getFileSize(File sourceFile) {
		long fileSize = -1L;
		FileInputStream fis = null;
		try{
			fis = new FileInputStream(sourceFile);
			fileSize = fis.available();
		}catch (IOException e){
			fileSize = -1L;
		}finally{
			if(fis != null){
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return fileSize;
	}
	
	/**
	 * 删除file,文件文件夹都可以
	 * @param rootPath 相对路径的根路径
	 * @param relativePath 文件的相对路径
	 * @author kakausn
	 */
	public static void deleteFileOrDirectory(String rootPath, String relativePath) {
		relativePath = relativePath.trim();
		if(relativePath == null || relativePath.length() == 0){
			return;
		}
		rootPath = rootPath.trim();
		if (rootPath == null || rootPath.length() == 0) {
			deleteFileOrDirectory(relativePath);
		} else {
			rootPath = formatFilePath(rootPath);
			if(!rootPath.endsWith("/")){
				rootPath += "/";
			}
			deleteFileOrDirectory(rootPath + relativePath);
		}
	}

	/**
	 * 删除文件或文件夹
	 * @param filePath
	 */
	public static void deleteFileOrDirectory(String filePath) {
		filePath = formatFilePath(filePath);
		File realFile = new File(filePath);
		deleteFileOrDirectory(realFile);
	}

	/**
	 * 删除file，文件文件夹都可以
	 * @param file
	 */
	public static void deleteFileOrDirectory(File file) {
		if (!file.exists()) {
			return;
		}
		if(file.isFile()){
			logger.debug("正在删除文件："+file.getAbsolutePath());
			if(!file.delete()){				
				dQue.offer( new DeleteFileTask(file,1000*30L));
			}
			return;
		}
		//储存递归的文件夹队列
		Queue<File> queue = new LinkedList<File>();
		//储存待删除的文件夹栈
		Deque<File> deque = new LinkedList<File>();
		queue.offer(file);
		while(queue.size()>0){
			File f = queue.poll();
			if(f.isFile()){
				logger.debug("正在删除文件："+f.getAbsolutePath());
				//删除文件
				if(!f.delete()){
					dQue.add(new DeleteFileTask(file, 1000*30));
					return;
				}
			}else{
				deque.push(f);
				File[] list = f.listFiles();
				for(File sub:list){
					if(sub.isFile()){
						logger.debug("正在删除文件："+sub.getAbsolutePath());
						//删除文件
						if(!sub.delete()){
							dQue.add(new DeleteFileTask(file, 1000*30));
							return;
						}
					}else{
						//文件夹放入队列再次递归
						queue.offer(sub);
					}
				}
			}
		}
		while(deque.size()>0){
			File f = deque.pop();
			logger.debug("正在删除文件夹："+f.getAbsolutePath());
			if(!f.delete()){
				dQue.add(new DeleteFileTask(file, 1000*30));
				return;
			}
		}
	}
	
	/**
	 * 删除文件或文件夹，删除失败不再重试
	 * @param file
	 * @author kakasun
	 */
	public static void deleteFileOrDirectoryNotAgine(File file) {
		if (!file.exists()) {
			return;
		}
		if(file.isFile()){
			file.delete();
			return;
		}
		//储存递归的文件夹队列
		Queue<File> queue = new LinkedList<File>();
		//储存待删除的文件夹栈
		Deque<File> deque = new LinkedList<File>();
		queue.offer(file);
		//逐层递归子文件夹
		while(queue.size()>0){
			File f = queue.poll();
			if(f.isFile()){
				//删除文件
				f.delete();
			}else{
				deque.push(f);
				File[] list = f.listFiles();
				for(File sub:list){
					if(sub.isFile()){
						//删除文件
						sub.delete();
					}else{
						//文件夹放入队列再次递归
						queue.offer(sub);
					}
				}
			}
		}
		//逐层删除子文件夹
		while(deque.size()>0){
			deque.pop().delete();
		}
	}

	/**
	 * 判断文件夹及其子文件夹 是否为空
	 * @param dir
	 * @return
	 */
	public static boolean isEmptyDir(File dir) {
		if(!dir.exists()){
			return true;
		}
		Queue<File> queue = new LinkedList<File>();
		queue.offer(dir);
		while(queue.size()>0){
			File file = queue.poll();
			if(file.isFile()){
				return false;
			}
			File[] list = file.listFiles();
			for(File f : list){
				if(f.isFile()){
					return false;
				}else{
					queue.offer(f);
				}
			}
		}
		return true;
	}

	/**
	 * 使用apache的zip工具包解压.zip文件
	 * @param zipFilePath zip文件路径
	 * @param outputDirectory 解压文件放置路径
	 */
	public static void unZip(String zipFilePath, String outputDirectory)
			throws IOException {
		unZip(zipFilePath, outputDirectory, null);
	}

	/**
	 * 使用apache的zip工具包解压.zip文件
	 * @param zipFilePath zip文件路径
	 * @param outputDirectory 解压文件放置路径
	 * @param encoder 编码格式 可以为
	 * @throws IOException 
	 */
	public static void unZip(String zipFilePath, String outputDirectory,
			String encoder) throws IOException {
		zipFilePath = zipFilePath.trim();
		formatFilePath(zipFilePath);
		outputDirectory = outputDirectory.trim();
		formatFilePath(outputDirectory);
		ZipFile zipFile = null;
		try {
			if (encoder != null && encoder.trim().length() > 0) {
				zipFile = new ZipFile(zipFilePath, encoder.trim());
			} else {
				zipFile = new ZipFile(zipFilePath);
			}
			unZip(zipFile, outputDirectory);
		} finally {
			if (zipFile != null) {
				zipFile.close();
			}
		}
	}
	
	/**
	 * 解压zip压缩文件
	 * @param zipFile zip压缩文件对象
	 * @param outputDirectory 解压文件输出文件夹
	 * @param encoder 压缩文件编码格式
	 * @throws IOException
	 */
	private static void unZip(ZipFile zipFile, String outputDirectory) throws IOException {
		// 获取迭代器
		Enumeration<ZipEntry> e = zipFile.getEntries();
		if (!outputDirectory.endsWith("/")) {
			outputDirectory += "/";
		}
		new File(outputDirectory).mkdirs();
		byte[] buffer = new byte[BUF_SIZE];
		// 遍历压缩文件中的file
		while (e.hasMoreElements()) {
			ZipEntry zipEntry = e.nextElement();
			String path = zipEntry.getName();
			formatFilePath(path);
			logger.debug(path);
			if (zipEntry.isDirectory()) {
				// 是文件夹，创建文件夹
				new File(outputDirectory + path).mkdirs();
				continue;
			}
			File f = new File(outputDirectory + path);
			creatFileIfNotExists(f);
			InputStream in = null;
			FileOutputStream out = null;
			try {
				in = zipFile.getInputStream(zipEntry);
				out = new FileOutputStream(f);
				int c = -1;
				while ((c = in.read(buffer)) != -1) {
					out.write(buffer, 0, c);
				}
			} finally {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			}
		}
	}

	/**
	 * 压缩单个文件或目录下的所有文件成zip格式 当压缩目录下的文件时，不包含该目录本身
	 * @param inputFilePath
	 * @param outputFileName
	 * @param encode 问价压缩中文字符集
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void zipFile(String inputFilePath, String outputFilePath,String encode) throws IOException {
		File inputFile = new File(inputFilePath);
		File outFile = new File(outputFilePath);
		zipFile(inputFile,outFile,encode);
	}
	
	/**
	 * 压缩单个文件或目录下的所有文件成zip格式 当压缩目录下的文件时，不包含该目录本身
	 * @param inputFilePath
	 * @param outputFileName
	 * @throws IOException
	 */
	public static void zipFile(File inputFile, File outputFile,String encode) throws IOException {
		ZipOutputStream zipOut = null;
		creatFileIfNotExists(outputFile);
		try {
			zipOut = new ZipOutputStream(new FileOutputStream(outputFile));
			zipOut.setEncoding(encode);
			String inputFilePath = inputFile.getAbsolutePath();
			if(!inputFilePath.endsWith("/")){
				inputFilePath += "/";
			}
			zip(zipOut, inputFile, inputFilePath.length());
		} finally {
			if (zipOut != null) {
				zipOut.close();
			}
		}
	}
	
	/**
	 * 压缩单个文件或目录下的所有文件成zip格式 当压缩目录下的文件时，不包含该目录本身
	 * @param inputFilePath
	 * @param outputFileName
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void zipFile(String inputFilePath, String outputFilePath) throws IOException {
		File inputFile = new File(inputFilePath);
		File outFile = new File(outputFilePath);
		zipFile(inputFile,outFile);
	}
	
	/**
	 * 压缩单个文件或目录下的所有文件成zip格式 当压缩目录下的文件时，不包含该目录本身
	 * @param inputFilePath
	 * @param outputFileName
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void zipFile(File inputFile, File outputFile) throws IOException {
		ZipOutputStream zipOut = null;
		try {
			zipOut = new ZipOutputStream(new FileOutputStream(outputFile));
			String inputFilePath = inputFile.getAbsolutePath();
			if(!inputFilePath.endsWith("/")){
				inputFilePath += "/";
			}
			zip(zipOut, inputFile, inputFilePath.length());
		} finally {
			if (zipOut != null) {
				zipOut.close();
			}
		}
	}

	/**
	 * 压缩文件或文件夹
	 * @param out
	 * @param file
	 * @param base
	 * @throws IOException
	 */
	private static void zip(ZipOutputStream out, File file, int base)
			throws IOException {
		Queue<File> queue = new LinkedList<File>();
		queue.offer(file);
		while (queue.size() > 0) {
			File f = queue.poll();
			if (f.isFile()) {
				addZipEntry(out, file, base);
				continue;
			}
			File[] list = f.listFiles();
			for (File sub : list) {
				if (sub.isFile()) {
					addZipEntry(out, sub, base);
					continue;
				}
				queue.offer(sub);
			}
		}

	}
	
	/**
	 * zip压缩文件
	 * @param out
	 * @param f
	 * @param base
	 * @throws IOException
	 */
	private static void addZipEntry(ZipOutputStream out, File f, int base)
			throws IOException {
		byte[] buffer = new byte[BUF_SIZE];
		String ap = f.getAbsolutePath();
		logger.debug("正在压缩文件："+ap);
		String fileName = ap.substring(base,ap.length());
		out.putNextEntry(new ZipEntry(fileName));
		int length = -1;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
			while ((length = fis.read(buffer)) != -1) {
				out.write(buffer, 0, length);
			}
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
	}

	/**
	 * 将字节数据写入文件
	 * @param file
	 * @param data
	 * @throws IOException
	 */
	public static void writeFile(File file, byte[] data) throws IOException {
		creatFileIfNotExists(file);
		OutputStream out = new FileOutputStream(file);
		try {
			out.write(data);
			out.flush();
		} finally {
			out.close();
		}
	}

	/**
	 * 读取传入的字节流，返回读取到的字节数组
	 * @param is 输入流
	 * @author kakasun
	 * @return 字节数组
	 * @throws IOException
	 */
	public static byte[] getBytes(InputStream is) throws IOException {
		byte[] buffer = new byte[BUF_SIZE];// 创建缓冲区
		int read = -1;
		// 创建字节缓存输出流
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			while ((read = is.read(buffer)) != -1) {
				// 读取文件的字节数据写入缓存流中
				bos.write(buffer, 0, read);
			}
			// 将缓存流中的字节数据转换为byte数组返回
			return bos.toByteArray();
		} finally {
			bos.close();
		}
	}

	/**
	 * 替换掉文件命名中不能使用的字符
	 * */
	public static String replaceCharacterFromFileName(String filename) {
		filename = filename.replaceAll("\\\\", "%");
		filename = filename.replaceAll("/", "%");
		filename = filename.replaceAll(":", "%");
		filename = filename.replaceAll("\\*", "%");
		filename = filename.replaceAll("\\?", "%");
		filename = filename.replaceAll("\"", "%");
		filename = filename.replaceAll("<", "%");
		filename = filename.replaceAll(">", "%");
		filename = filename.replaceAll("\\|", "%");
		return filename;
	}
	/**
	 * 格式化文件路径
	 * @Title: formatFilePath 
	 * @Description: 
	 * @param filePath
	 * @return 
	 * @return String 
	 * @author kakasun 
	 * @date 2017年3月7日下午3:20:04
	 */
	public static String formatFilePath(String filePath){
		return filePath.replaceAll("[\\\\/]+", "/");
	}

	/**
	 * 判断文件夹路径是否以  / 结尾，不是则添加
	 * @Title: addSeparator 
	 * @Description: 
	 * @param directoryPath
	 * @return 
	 * @return String 
	 * @author kakasun 
	 * @date 2017年3月7日下午3:22:41
	 */
	public static String addSeparator(String directoryPath){
		if(!directoryPath.endsWith("/")){
			directoryPath += "/";
		}
		return directoryPath;
	}
	public static void main(String[] args) {
		try {
			String source = "C:\\Users\\\\//once\\.myeclipse\\\\/libs\\/derby_10.10.2.0//";
			System.out.println(formatFilePath(source));
//			String tagert = "K:/";
//			String deletePath = "k:/Tencent.zip";
//			copyDirectiory(source, tagert);
//			zipFile(source, deletePath);
//			unZip(deletePath, tagert);
			System.exit(-1);
//			System.out.println(System.currentTimeMillis());
//			dQue.add(new DeleteFileTask(new File(deletePath), 1000*3));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
