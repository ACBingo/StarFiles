package com.example.acbingo.myfinaltest10.BluetoothService.BlutoothMainAction;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.example.acbingo.myfinaltest10.BluetoothService.btfiletranform.FileWriteService;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class FileReciver extends Thread {
	private static final String TAG = "FileSender";
	private final BluetoothSocket mmSocket;
	private final InputStream mmInStream;
	private final OutputStream mmOutStream;
	private BluetoothChatService bcs;
	public static int AcceptFlag ;

	public FileReciver(BluetoothSocket socket, String socketType , BluetoothChatService Bcs) {
		//Log.d(TAG, "create ConnectedThread: " + socketType);
		mmSocket = socket;
		InputStream tmpIn = null;
		OutputStream tmpOut = null;
		bcs = Bcs;
		try {
			tmpIn = socket.getInputStream();
			tmpOut = socket.getOutputStream();
		} catch (IOException e) {
			//Log.e(TAG, "temp sockets not created", e);
		}

		mmInStream = tmpIn;
		mmOutStream = tmpOut;
	}

	public void run() {
		Log.i(TAG, "BEGIN mConnectedThread");
		byte[] buffer = new byte[1024];
		long hasread = 0;
		while (true) {
			try {
				int nowlen = mmInStream.read(buffer);
				String fileName = new String(buffer,0,nowlen, "utf-8");

				nowlen = mmInStream.read(buffer);
				long fileLen = Integer.valueOf(new String(buffer,0,nowlen, "utf-8"));

				//弹框要求确认是否接收
				Message msg = bcs.mHandler
						.obtainMessage(BluetoothChat.MESSAGE_REQUESTACCEPTFILE);
				Bundle bundle = new Bundle();
				bundle.putString(BluetoothChat.TOAST,
						fileName + " 大小为：" + fileLen);
				msg.setData(bundle);
				bcs.mHandler.sendMessage(msg);

				//等待结果
				AcceptFlag = 0;
				while(AcceptFlag == 0);

				//如果用户拒绝接收则直接发送一条拒绝接收的信息，然后从新进入等待接收文件状态
				if(AcceptFlag == 2){
					write(FileSender.DISACCEPT.getBytes());
					continue;
				}
				write("ACCEPT".getBytes());

				/* 获取文件需要存放位置下的流，将文件写进去 */
				BufferedOutputStream fdis = FileWriteService
						.setFileWriteService(fileName, fileLen);

				int percentage = 0;
				while (true){
					int readlen = 0;
					if (mmInStream != null){
						readlen = mmInStream.read(buffer);
					}

					if (readlen == -1)
						break;

					hasread += readlen;

					Message msg2 = new Message();
					msg2.what = BluetoothChat.MESSAGE_UPDATE_WAVELOAD;
					msg2.arg1 = (int) (1.0 * hasread / fileLen * 100.0);
					bcs.mHandler.sendMessage(msg2);

					Log.d("heheda","--> "+msg2.arg1 + " " + hasread + " "+ fileLen);

					fdis.write(buffer, 0, readlen);
					if (hasread >= fileLen) {
						break;
					}
				}
				fdis.flush();
				fdis.close();
				mmSocket.close();
			} catch (IOException e) {
				//Log.e(TAG, "disconnected", e);
				bcs.connectionLost();
				bcs.start();
				break;
			}
		}
		Message msg2 = new Message();
		msg2.what = BluetoothChat.MESSAGE_END_AS_RECEIVER;
		bcs.mHandler.sendMessage(msg2);
	}

	/* 文件传输函数 */
	public void WriteFile(String FilePath) {
		File te = new File(FilePath);
		write(te.getName().getBytes());
		write(String.valueOf(te.length()).getBytes());
		try {
			mmOutStream.flush();
			FileInputStream fis = new FileInputStream(te);
			BufferedInputStream bis = new BufferedInputStream(fis);
			byte[] buf = new byte[1024];
			int len ;
			while ((len = bis.read(buf)) != -1) {
				mmOutStream.write(buf ,  0, len);
			}
			mmOutStream.flush();
			bis.close();
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* 只写一句简短的串，并清空缓存，相当于将这一句话，一次性传递过去 */
	public void write(byte[] buffer) {
		try {
			mmOutStream.write(buffer);
			mmOutStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
			//Log.e(TAG, "Exception during write", e);
		}
	}

	public void cancel() {
		try {
			mmSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
			//Log.e(TAG, "close() of connect socket failed", e);
		}
	}
}