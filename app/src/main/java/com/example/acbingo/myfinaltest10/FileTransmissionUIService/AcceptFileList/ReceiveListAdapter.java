package com.example.acbingo.myfinaltest10.FileTransmissionUIService.AcceptFileList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.acbingo.myfinaltest10.R;
import com.example.acbingo.myfinaltest10.activitys.ReceiveFileInfoActivity;

import java.util.List;

/**
 * Created by zhaoshuai on 2016/6/10.
 */
public class ReceiveListAdapter extends BaseAdapter {

    private List<AcceptInfo> mDates = null;
    private ReceiveFileInfoActivity mContext;

    private class ZuJian{
        int id;
        TextView name;
        TextView size;
        TextView downloadPercent;
        ProgressBar bar;
    };
    public ReceiveListAdapter(Context context , List<AcceptInfo> data) {
        this.mContext = (ReceiveFileInfoActivity)context;
        mDates = data;
    }

    @Override
    public int getCount() {
        return mDates.size();
    }

    @Override
    public Object getItem(int position) {
        return mDates.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setDates(List<AcceptInfo> mDates) {
        this.mDates = mDates;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ZuJian zujian;
        if (convertView == null) {
            zujian = new ZuJian();
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.accept_line, null);
            zujian.size = (TextView) convertView.findViewById(R.id.accept_line_size);
            zujian.name = (TextView) convertView.findViewById(R.id.accept_line_name);
            zujian.downloadPercent = (TextView) convertView.findViewById(R.id.accept_line_download_percent);
            zujian.bar = (ProgressBar) convertView.findViewById(R.id.accept_line_progressbar);
            convertView.setTag(zujian);
        } else {
            zujian = (ZuJian) convertView.getTag();
        }
        setData(zujian, position);
        return convertView;
    }

    /**
     * 设置viewHolder的数据
     //* @param holder
     //* @param itemIndex
     */
    private void setData(final ZuJian zujian, int itemIndex) {
        AcceptInfo info = mDates.get(itemIndex);
        zujian.id = info.getId();
        zujian.name.setText(info.getName());
        zujian.size.setText(info.getSize());
        /*
            当处于PENDING  , COMPLETE , START 这些双端状态时不显示进度条  ， 此时downloadpercent 作为状态显示空间
            进度条只在文件传输过程中显示 ， 并用downloadpercent 作为辅助进度提示
        */
        if(info.isNotinTransformationStatus() == false ){
            zujian.downloadPercent.setText(info.getDownloadPercent());
            zujian.bar.setVisibility(View.INVISIBLE);
        }  else {
            zujian.downloadPercent.setText("下载进度：" + Integer.valueOf(info.getDownloadPercent()) + "%");
            zujian.bar.setVisibility(View.VISIBLE);
            zujian.bar.setProgress(Integer.valueOf(info.getDownloadPercent()));
        }
    }


    /**
     * 局部刷新
     * @param view
     * @param itemIndex
     */
    public void updateView(View view, int itemIndex) {
        if(view == null) {
            return;
        }
        //从view中取得holder
        ZuJian holder = (ZuJian) view.getTag();
        //holder.statusIcon = (DownloadPercentView) view.findViewById(R.id.status_icon);
        holder.name = (TextView) view.findViewById(R.id.accept_line_name);
        holder.downloadPercent = (TextView) view.findViewById(R.id.accept_line_download_percent);
        holder.bar = (ProgressBar) view.findViewById(R.id.accept_line_progressbar);
        holder.size = (TextView) view.findViewById(R.id.accept_line_size);
        setData(holder, itemIndex);
    }

//    /**
//     * 根据状态设置图标
//     * @param downloadPercentView
//     * @param status
//     */
//    private void setIconByStatus(DownloadPercentView downloadPercentView, AppContent.Status status) {
//        downloadPercentView.setVisibility(View.VISIBLE);
//        if(status == AppContent.Status.PENDING) {
//            downloadPercentView.setStatus(DownloadPercentView.STATUS_PEDDING);
//        }
//        if(status == AppContent.Status.DOWNLOADING) {
//            downloadPercentView.setStatus(DownloadPercentView.STATUS_DOWNLOADING);
//        }
//        if(status == AppContent.Status.WAITING) {
//            downloadPercentView.setStatus(DownloadPercentView.STATUS_WAITING);
//        }
//        if(status == AppContent.Status.PAUSED) {
//            downloadPercentView.setStatus(DownloadPercentView.STATUS_PAUSED);
//        }
//        if(status == AppContent.Status.FINISHED) {
//            downloadPercentView.setStatus(DownloadPercentView.STATUS_FINISHED);
//        }
//    }
}
