package com.example.acbingo.myfinaltest10.FileChooseService;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.acbingo.myfinaltest10.R;

import java.util.List;
import java.util.Map;

public class ChooseFileAdapter extends BaseAdapter {

    private List<Map<String, Object>> data;
    private LayoutInflater layoutInflater;
    private final ChooseFileActivity context;
    public ChooseFileAdapter(Context context, List<Map<String, Object>> data){
        this.context=(ChooseFileActivity)context;
        this.data=data;
        this.layoutInflater=LayoutInflater.from(context);
    }
    /**
     * ������ϣ���Ӧlist.xml�еĿؼ�
     * @author Administrator
     */
    public final class Zujian{
        public ImageView image;
        public TextView title;
        public ImageView view;
    }

    @Override
    public int getCount() {
        return data.size();
    }
    /**
     * ���ĳһλ�õ����
     */
    @Override
    public Object getItem(int position) {
        return data.get(position);
    }
    /**
     * ���Ψһ��ʶ
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Zujian zujian;
        if(convertView==null){
            zujian=new Zujian();
            convertView=layoutInflater.inflate(R.layout.choose_file_adapter_line, null);
            zujian.image=(ImageView)convertView.findViewById(R.id.line_image);
            zujian.title=(TextView)convertView.findViewById(R.id.line_title);
            zujian.view=(ImageView)convertView.findViewById(R.id.line_button);
            convertView.setTag(zujian);
        }else{
            zujian=(Zujian)convertView.getTag();
        }
        ViewInfo info = new ViewInfo();
        if(this.context.isListViewAtPositionisSelected(position) == true)
            info.setSelected(true);
        info.setFilePath((String)data.get(position).get("filepath"));
        info.setIsDir((Boolean) data.get(position).get("isfile"));
        zujian.view.setTag(info);
        if(info.isIsFile() == false)
            zujian.view.setVisibility(View.INVISIBLE);
        else    zujian.view.setVisibility(View.VISIBLE);
        zujian.image.setBackgroundResource((Integer)data.get(position).get("image"));
        zujian.title.setText((String)data.get(position).get("title"));
        zujian.view.setImageResource(
                ((ViewInfo)zujian.view.getTag()).isSelected() == true ?
                        R.drawable.btn_check_on_holo_light : R.drawable.btn_check_off_holo_light);
        zujian.view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //showInfo(position);
                ViewInfo inf = (ViewInfo)zujian.view.getTag();
                inf.setSelected(!inf.isSelected());
                if(inf.isSelected() == true){
                    context.addSelectedPath(inf.getFilePath());
                    zujian.view.setImageResource(R.drawable.btn_check_on_holo_light);
                } else {
                    context.removeSelectedPath(inf.getFilePath());
                    zujian.view.setImageResource(R.drawable.btn_check_off_holo_light);
                }
            }
        });
        return convertView;
    }
    public void showInfo(int position){

        //ImageView img=new ImageView(ListViewActivity.this);
        //img.setImageResource(R.drawable.b);
        new AlertDialog.Builder((Activity)context)
                .setTitle("dsfsd" + position)
                .setMessage("just for test")
                .setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }
}