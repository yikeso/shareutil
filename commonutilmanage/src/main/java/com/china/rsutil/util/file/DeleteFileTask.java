package com.china.rsutil.util.file;

import java.io.File;
import java.io.Serializable;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DeleteFileTask implements Delayed, Runnable, Serializable {

	private static final long serialVersionUID = 1L;

	private long deleteTime;
	private File file;

	/**
	 * 多长时间后删除，单位毫秒
	 * 
	 * @param later
	 */
	public DeleteFileTask(File file, long later) {
		this.deleteTime = System.currentTimeMillis() + later;
		this.file = file;
	}

	public DeleteFileTask(String filePath, long later) {
		this.deleteTime = System.currentTimeMillis() + later;
		this.file = new File(filePath);
	}

	@Override
	public int compareTo(Delayed deleteTask) {
		if (deleteTask == null) {
			return 1;
		}
		if (deleteTask == this) {
			return 0;
		}
		if (!(deleteTask instanceof DeleteFileTask)) {
			return 1;
		}

		DeleteFileTask task = (DeleteFileTask) deleteTask;
		if (this.deleteTime > task.getDeleteTime()) {
			return 1;
		} else if (this.deleteTime == task.getDeleteTime()) {
			return 0;
		} else {
			return -1;
		}
	}

	@Override
	public long getDelay(TimeUnit arg0) {
		return arg0.convert(deleteTime - System.currentTimeMillis(),
				TimeUnit.MILLISECONDS);
	}

	@Override
	public void run() {
		FileUtils.deleteFileOrDirectoryNotAgine(file);
	}

	public long getDeleteTime() {
		return deleteTime;
	}

}
