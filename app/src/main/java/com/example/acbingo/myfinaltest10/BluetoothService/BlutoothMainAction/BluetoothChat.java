package com.example.acbingo.myfinaltest10.BluetoothService.BlutoothMainAction;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.example.acbingo.myfinaltest10.BluetoothService.btfiletranform.FileWriteService;
import com.example.acbingo.myfinaltest10.R;
import com.example.acbingo.myfinaltest10.FileChooseService.ChooseFileActivity;
import com.example.acbingo.myfinaltest10.FileChooseService.FileChoose;
import com.example.acbingo.myfinaltest10.activitys.ReceiveFileEndActivity;
import com.example.acbingo.myfinaltest10.activitys.SendFileEndActivity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import me.itangqi.waveloadingview.WaveLoadingView;
import su.levenetc.android.badgeview.BadgeView;

public class BluetoothChat extends Activity {

	private static final String TAG = "BluetoothChat";
	private static final boolean D = true;

	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final int MESSAGE_REQUESTACCEPTFILE = 6;
	public static final int MESSAGE_REQUESTACCEPTFILE_REPEAT = 7;
	public static final int MESSAGE_SENDFILE = 8;
	public static final int MESSAGE_UPDATE_WAVELOAD = 9;
	public static final int MESSAGE_END_AS_RECEIVER = 10;
	public static final int MESSAGE_END_AS_SENDER= 11;

	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	private static final int REQUEST_CONNECT_DEVICE = 2;
	private static final int REQUEST_ENABLE_BT = 3;
	private static final int REQUESTCODE = 1;
	public  static final int CONNECTED_AS_RECEIVER = 66;
	public  static final int CONNECTED_AS_SENDER = 77;
	public  static final int ENSURE_DISCOVERAVLE= 88;

	private Button mSendButton;
	private Button mPairedButton;
	private Button mChoosefileButton;
	private Button ensurediscoverButton;
	private BadgeView badgeView;
	private WaveLoadingView waveload ;

	private String mConnectedDeviceName = null;
	private ArrayAdapter<String> mConversationArrayAdapter;
	private BluetoothAdapter mBluetoothAdapter = null;

	private BluetoothChatHandlerService mchathandlersevice = new BluetoothChatHandlerService();
	private AlertDialog mydialog;
	private Activity nowcontext;
	private final FileWriteService myfilewriteservice = null;
	private BluetoothChatService mChatService = null;
	private TimeCount time;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D)
			Log.e(TAG, "+++ ON CREATE +++");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_buletooth_main);

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		nowcontext = this;
		initView();
		/*
		  下面这句话适用于在打开该app之前蓝牙设备已经打开的情况
		*/
		if(mBluetoothAdapter.isEnabled() == true){
			this.mChatService = new BluetoothChatService(this, this.mHandler);
		}
	}

	void initView() {
		mPairedButton = (Button) findViewById(R.id.peidui_btn);
		mChoosefileButton = (Button) findViewById(R.id.bluetooth_main_choosefile_btn);
		mSendButton = (Button) findViewById(R.id.bluetooth_main_btn_send);
		ensurediscoverButton =(Button) findViewById(R.id.ensure_discoverable_btn);
		badgeView = (BadgeView) findViewById(R.id.badge_view);
		waveload = (WaveLoadingView) findViewById(R.id.waveLoadingView);
		mSendButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				    if(FileChoose.getFileChooseCount() == 0){
						    Toast.makeText(getApplicationContext(),"您还没有选择任何文件偶",Toast.LENGTH_SHORT).show();
						    return ;
					}
				    if(mChatService.getState() != mChatService.STATE_CONNECTED){
						  Toast.makeText(getApplicationContext(),"您还没有和其他设备建立连接哦",Toast.LENGTH_SHORT).show();
					      return ;
					}
					try {
						mChatService.WriteFile(FileChoose.getFileChooseArrayList().get(0).getCanonicalFile().toString());
						FileChoose.getFileChooseArrayList().clear();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		});
        mChoosefileButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(),"目前只支持同时发送一个文件 ， 默认为您选择的第一个文件",Toast.LENGTH_SHORT).show();
				Intent te = new Intent(BluetoothChat.this, ChooseFileActivity.class);
				startActivityForResult(te, REQUESTCODE);
			}
		});
		mPairedButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent serverIntent = new Intent(getApplicationContext(), BlueDeviceListActivity.class);
				startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			}
		});
		time = new TimeCount(180000, 1000);
		ensurediscoverButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				 ensureDiscoverable();
				 time.start();
			}
		});
		mConversationArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.bluethooth_conversation_message);
	}

	@Override
	/*
	 * onstart这里先确认蓝牙是否打开，如果没有打开
	 */
	public void onStart() {
		super.onStart();
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();

		if (mChatService != null){
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				mChatService.start();
			}
		}
	}

	// 只有当蓝牙打开后执行该动作，初始化聊天服务设备
	private void setupChat() {
		while (!this.mBluetoothAdapter.isEnabled()) ;
		this.mChatService = new BluetoothChatService(this, this.mHandler);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mChatService != null)
			mChatService.stop();
	}

	private void ensureDiscoverable() {
		if (D)
			Log.d(TAG, "ensure discoverable");
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 180);
			startActivity(discoverableIntent);
		}
	}

	private final void setStatus(int resId) {
		//final ActionBar actionBar = getActionBar();
		//actionBar.setSubtitle(resId);
		badgeView.setValue(getResources().getString(resId));
	}

	private final void setStatus(CharSequence subTitle) {
		//final ActionBar actionBar = getActionBar();
		//actionBar.setSubtitle(subTitle);
		badgeView.setValue(subTitle.toString());
	}

	/**
	 *
	 */
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MESSAGE_STATE_CHANGE:
					if (D)
						Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
					switch (msg.arg1) {
				/*
				 * 刚刚连接到某个设备，菜单栏当前的状态设置为 连接到：某某设备
				 */
						case BluetoothChatService.STATE_CONNECTED:
							setStatus(getString(R.string.title_connected_to,
									mConnectedDeviceName));
							mConversationArrayAdapter.clear();
							break;
				/*
				 * 正在连接某个设备，将菜单栏的当前状态设置为 正在连接...
				 */
						case BluetoothChatService.STATE_CONNECTING:
							setStatus(R.string.title_connecting);
							break;
				/*
				 * 正在监听或无连接状态 设置菜单栏当前状态为 ： 无连接
				 */
						case BluetoothChatService.STATE_LISTEN:
						case BluetoothChatService.STATE_NONE:
							setStatus(R.string.title_not_connected);
							break;
					}
					break;
			/*
			 * 自己写出一句话，要发给对方时，也要更新自己的对话列表，将说的话添加到listview的适配数组里面
			 */
				case MESSAGE_WRITE:
					byte[] writeBuf = (byte[]) msg.obj;
					String writeMessage;
					try {
						writeMessage = new String(writeBuf, "utf-8");
						mConversationArrayAdapter.add("Me:  " + writeMessage);
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
			/*
			 * 连接方发过来一条对话，更新对话列表
			 */
				case MESSAGE_READ:
					String readMessage;
					try {
						readMessage = msg.getData().getString(TOAST);
						mConversationArrayAdapter.add(mConnectedDeviceName + ":  "
								+ readMessage);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
			/*
			 * 传来时刻： 两个蓝牙设备已经配对成功，在配对列表中点击某设备要连接时，收到这条信息
			 * 效果：将mConnectedDeviceName设置为传递过来的设备名称
			 */
				case MESSAGE_DEVICE_NAME:
					mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
					Toast.makeText(getApplicationContext(),
							"Connected to " + mConnectedDeviceName,
							Toast.LENGTH_SHORT).show();
					break;
			/*
			 * 时刻：任意 效果：TOAST传递过来的字符串
			 */
				case MESSAGE_TOAST:
					Toast.makeText(getApplicationContext(),
							msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
							.show();
					break;
				case MESSAGE_REQUESTACCEPTFILE:
					new BluetoothChatHandlerService().RequestAcceptFile
							(mChatService.mFileReciver, (String)msg.getData().getString(TOAST) , BluetoothChat.this);
					break;
				case MESSAGE_UPDATE_WAVELOAD:
					int val = msg.arg1;
					waveload.setProgressValue(val);
					waveload.setCenterTitle(
							String.valueOf(val)+" %"
					);
					break;
				case MESSAGE_END_AS_RECEIVER:
					Intent te = new Intent(getApplicationContext(), ReceiveFileEndActivity.class);
					startActivity(te);
					BluetoothChat.this.finish();
					break;
				case MESSAGE_END_AS_SENDER:
					Intent te2 = new Intent(getApplicationContext(), SendFileEndActivity.class);
					startActivity(te2);
					BluetoothChat.this.finish();
					break;
				case CONNECTED_AS_RECEIVER:
					BluetoothChat.this.mSendButton.setEnabled(false);
				    break;
				case CONNECTED_AS_SENDER:
					BluetoothChat.this.mSendButton.setEnabled(true);
					break;
				case ENSURE_DISCOVERAVLE :
					ensureDiscoverable();
					break;
			}
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
			case REQUEST_CONNECT_DEVICE:
				if (resultCode == Activity.RESULT_OK) {
					connectDevice(data);
				}
				break;
			case REQUEST_ENABLE_BT:
				if (resultCode == Activity.RESULT_OK) {
					setupChat();
				} else {
					Log.d(TAG, "BT not enabled");
					Toast.makeText(this, R.string.bt_not_enabled_leaving,
							Toast.LENGTH_SHORT).show();
					finish();
				}
				break;
		/*
		 * 时刻：当点击发送按钮后，进入文件浏览模式，浏览完毕返回选中的文件路径
		 * 作用：将选中的路径，文件名和文件长度发送到连接方作为一个接收文件的请求信息
		 */
			case REQUESTCODE:
				if (resultCode == Activity.RESULT_OK) {
					ArrayList<String> path = data.getExtras().getStringArrayList(ChooseFileActivity.ALLSELECTEDPATH);
					FileChoose te = FileChoose.getInstance();
					FileChoose.getFileChooseArrayList().clear();
				    if(path.size() > 0){
					  te.addFile(path.get(0));
					  Toast.makeText(this, "已经选择文件 " + path.get(0), Toast.LENGTH_LONG)
							  .show();
				    }

				}
				break;
		}
	}

	private void connectDevice(Intent data) {
		String address = data.getExtras().getString(
				BlueDeviceListActivity.EXTRA_DEVICE_ADDRESS);
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		mChatService.connect(device);
	}



	/**
	 * 继承倒计时类
	 * @author
	 */
	class TimeCount extends CountDownTimer {
		/**
		 * 构造方法
		 * @param millisInFuture
		 *            总倒计时长 毫秒
		 * @param countDownInterval
		 *            倒计时间隔
		 */
		public TimeCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}
		@Override
		public void onTick(long millisUntilFinished) {
			ensurediscoverButton.setEnabled(false);
			ensurediscoverButton.setText(millisUntilFinished / 1000 + "秒");
		}
		@Override
		public void onFinish() {// 计时结束
			ensurediscoverButton.setEnabled(true);
			ensurediscoverButton.setText("打开可见");
		}
	}
}
