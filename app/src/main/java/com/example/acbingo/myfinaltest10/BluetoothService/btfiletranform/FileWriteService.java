package com.example.acbingo.myfinaltest10.BluetoothService.btfiletranform;

import com.example.acbingo.myfinaltest10.activitys.ReceiveFileService;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;

public class FileWriteService {
	public static String DefaultRootPath = ReceiveFileService.getFileSavePath();// �����ļ���Ĭ�ϴ�Ÿ��ļ�����
	public static RandomAccessFile raf = null;
	static FileOutputStream fos = null;

	// public static long nowAcceptLength = 0;

	public static BufferedOutputStream setFileWriteService(String FileName,
														   long FileLength) {
		try {
			File te = new File(DefaultRootPath + FileName);
			if(!te.exists()){
				te.createNewFile();
			}
			BufferedOutputStream fileOut = new BufferedOutputStream(
					new FileOutputStream(te));
			return fileOut;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
