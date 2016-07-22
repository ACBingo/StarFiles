package com.example.acbingo.myfinaltest10.BluetoothService.BlutoothMainAction;

import android.bluetooth.BluetoothSocket;
import android.os.Message;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class FileSender{
	private static final String TAG = "FileSender";
	static final String ACCEPT = "accept";
	static final String DISACCEPT = "disaccept";
	private final BluetoothSocket mmSocket;
	private final InputStream mmInStream;
	private final OutputStream mmOutStream;
	private BluetoothChatService bcs;

	public FileSender(BluetoothSocket socket, String socketType , BluetoothChatService Bcs) {
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

	/* 文件传输函数 */
	public void WriteFile(String FilePath) {
		new FileSendThread(FilePath).start();
	}

	public void cancel() {
		try {
			mmSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
			//Log.e(TAG, "close() of connect socket failed", e);
		}
	}
	private class FileSendThread extends Thread{
		String path ;
		public FileSendThread(String filepath){
			path = filepath;
		}
		public void run(){
			File te = new File(path);
			long FileLen = te.length();
			long hasread = 0;
			write(te.getName().getBytes());
			write(String.valueOf(te.length()).getBytes());
			byte[] buf = new byte[1024];
			int len ;
			try {
				len = mmInStream.read(buf);
				String isAccept = new String(buf , 0 , len);
				if(isAccept.compareTo(DISACCEPT) == 0){
					return ;
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				mmOutStream.flush();
				FileInputStream fis = new FileInputStream(te);
				BufferedInputStream bis = new BufferedInputStream(fis);
				while ((len = bis.read(buf)) != -1) {
					mmOutStream.write(buf ,  0, len);
					hasread += len;

					Message msg2 = new Message();
					msg2.what = BluetoothChat.MESSAGE_UPDATE_WAVELOAD;
					msg2.arg1 = (int) (1.0 * hasread / FileLen * 100.0);
					bcs.mHandler.sendMessage(msg2);
					//Log.d("heheda","--> "+msg2.arg1  + hasread + " "+ FileLen);
				}
				mmOutStream.flush();
				bis.close();
				fis.close();
				mmSocket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			Message msg2 = new Message();
			msg2.what = BluetoothChat.MESSAGE_END_AS_SENDER;
			bcs.mHandler.sendMessage(msg2);
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
	}
}