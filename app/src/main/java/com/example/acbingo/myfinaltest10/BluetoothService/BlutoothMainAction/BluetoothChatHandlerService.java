package com.example.acbingo.myfinaltest10.BluetoothService.BlutoothMainAction;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

public class BluetoothChatHandlerService {
	public void RequestAcceptFile(final FileReciver reciver, String filename,
								  Context context) {
		Activity te = (Activity) context;
		Dialog d = new AlertDialog.Builder(context)
				.setTitle("警告")
				.setMessage("是否要接收文件:" + filename)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						reciver.AcceptFlag = 1;
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						reciver.AcceptFlag = 2;
					}
				}).setNeutralButton("忽略", null).show();

	}
}