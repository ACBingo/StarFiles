package com.example.acbingo.myfinaltest10.FileManagerService;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.acbingo.myfinaltest10.R;

import java.util.Date;

public class FileManagerListViewAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private FileManagerMainActivity mFileManagerMainActivity;
	UserOperate mUserOperate;
	Utils utils;

	public FileManagerListViewAdapter(FileManagerMainActivity fileManagerMainActivity, UserOperate userOperate) {
		mFileManagerMainActivity = fileManagerMainActivity;
		mUserOperate = userOperate;
		mInflater = LayoutInflater.from(fileManagerMainActivity);
		utils=new Utils();
	}

	/*
	    getView 推测 上拉 或者 下拉 过程中出现该item就会调用该函数
	    对函数参数的解释
	               position item在listview中的绝对位置
					   ConvertView 为对应的item的引用
	*/
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		FileManagerMainActivity.Print("hehe ---> " + position);
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.filemanager_listview_item, null);
			holder = new ViewHolder();
			holder.image = (ImageView) convertView.findViewById(R.id.itemImage);
			holder.title = (TextView) convertView.findViewById(R.id.itemTitle);
			holder.count = (TextView) convertView.findViewById(R.id.itemCount);
			holder.time = (TextView) convertView.findViewById(R.id.itemTime);
			holder.size = (TextView) convertView.findViewById(R.id.itemSize);
			holder.radio = (CheckBox) convertView
				.findViewById(R.id.itemChecked);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (mFileManagerMainActivity.getListItem().get(position).get(FileManager.IMAGE) instanceof Bitmap)
			holder.image.setImageBitmap((Bitmap) mFileManagerMainActivity.getListItem()
					.get(position).get(FileManager.IMAGE));
		else
			holder.image.setImageResource((Integer) (mFileManagerMainActivity
					.getListItem().get(position).get(FileManager.IMAGE)));
		holder.title.setText((String) mFileManagerMainActivity.getListItem().get(position)
				.get(FileManager.TITLE));
		holder.count.setText((String) mFileManagerMainActivity.getListItem().get(position)
				.get(FileManager.COUNT));
		holder.time.setText(utils.timeFormat((Date) mFileManagerMainActivity.getListItem()
				.get(position).get(FileManager.TIME)));
		if ((Boolean) mFileManagerMainActivity.getListItem().get(position)
				.get(FileManager.IS_DIR))
			holder.size.setText("");
		else
			holder.size.setText(utils.sizeAddUnit((Long) mFileManagerMainActivity.getListItem()
					.get(position).get(FileManager.SIZE)));
		if (mFileManagerMainActivity.isRadioVisible()) {
			holder.radio.setVisibility(View.VISIBLE);
			holder.radio.setChecked((Boolean) mFileManagerMainActivity.getListItem()
					.get(position).get(FileManager.IS_CHECKED));
			holder.radio.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mUserOperate.select(position);
				}
			});
		} else
			holder.radio.setVisibility(View.GONE);
		return convertView;
	}

	static class ViewHolder {
		private ImageView image;
		private TextView title;
		private TextView count;
		private TextView time;
		private TextView size;
		private CheckBox radio;
	}

	@Override
	public int getCount() {
		return mFileManagerMainActivity.getListItem().size();
	}

	@Override
	public Object getItem(int position) {
		return mFileManagerMainActivity.getListItem().get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
