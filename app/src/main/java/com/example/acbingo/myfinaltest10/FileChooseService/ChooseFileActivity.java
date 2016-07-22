package com.example.acbingo.myfinaltest10.FileChooseService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.acbingo.myfinaltest10.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChooseFileActivity extends Activity {

    public static final int CHOOSEFILEACTIVITYRESULT = 1;
    public static final String ALLSELECTEDPATH = "AllSelectedPath";
    private String SDCARD_ROOT = "/mnt/sdcard/";
    private String TOP_ROOT = "/";
    private final int MY_REQUESTCODE = 3;

    TextView text1;
    ListView list;
    Button btnback;
    File CurrentParent;
    File[] CurrentFiles;
    Button btnScanSelectPath;
    Button btnConfirm;
    ArrayList<String> AllSelectPath = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choosefile);

        findView();
        add_last_select_file();
        File root = new File(SDCARD_ROOT);
        if (root.exists()) {
            CurrentParent = root;
            CurrentFiles = root.listFiles();
            inflatelistView(CurrentFiles);
        }
        setListener();
    }

    private void add_last_select_file() {
        ArrayList<File> lastall = FileChoose.getFileChooseArrayList();
        for(File x : lastall){
            try {
                AllSelectPath.add(x.getCanonicalPath().toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setListener() {
        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                if (CurrentFiles[position].isFile()) {
                    return;
                }
                File[] tmp = CurrentFiles[position].listFiles();
                if (tmp == null || tmp.length == 0) {
                    Toast.makeText(ChooseFileActivity.this, "当前文件为空",
                            Toast.LENGTH_SHORT).show();
                } else {
                    CurrentParent = CurrentFiles[position];
                    CurrentFiles = tmp;
                    inflatelistView(CurrentFiles);
                }
            }
        });
        btnback.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                try {
                    if (!CurrentParent.getCanonicalPath().equals(SDCARD_ROOT)) {
                        CurrentParent = CurrentParent.getParentFile();
                        CurrentFiles = CurrentParent.listFiles();
                        inflatelistView(CurrentFiles);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        btnConfirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                finish_activity();
            }
        });
        btnScanSelectPath.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                try {
                    Intent te = new Intent(ChooseFileActivity.this,
                            PathScanActivity.class);
                    te.putStringArrayListExtra("pathlist", AllSelectPath);
                    startActivityForResult(te, MY_REQUESTCODE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }
    public void finish_activity(){
        try {
            Intent intent = new Intent();
            intent.putExtra(ALLSELECTEDPATH, AllSelectPath);
            setResult(RESULT_OK, intent);
            finish();
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case MY_REQUESTCODE:
                AllSelectPath = data.getStringArrayListExtra("pathlist");
                inflatelistView(this.CurrentFiles);
                break;
            default:
        }
    }

    private void inflatelistView(File[] files) {
        List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < files.length; i++) {
            Map<String, Object> listItem = new HashMap<String, Object>();
            if(files[i].getName().charAt(0) == '.')
                     continue;
            //以.开头的文件为隐藏文件不提供发送任务
            if (files[i].isDirectory()) {
                listItem.put("image", R.drawable.folder);
            } else {
                listItem.put("image", R.drawable.file_icon_default);
            }
            listItem.put("title", files[i].getName());
            listItem.put("isfile" ,files[i].isFile());
            try {
                listItem.put("filepath", files[i].getCanonicalPath());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            listItems.add(listItem);
        }
        list.setAdapter(new ChooseFileAdapter(this, listItems));
        try {
            text1.setText("当前路径为：" + CurrentParent.getCanonicalPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void findView() {
        text1 = (TextView) findViewById(R.id.textView1);
        list = (ListView) findViewById(R.id.list);
        btnback = (Button) findViewById(R.id.parent);
        btnConfirm = (Button) findViewById(R.id.confirm);
        btnScanSelectPath = (Button) findViewById(R.id.SelectedPathList);
    }
    public void addSelectedPath(String path){
        AllSelectPath.add(path);
        /*Toast.makeText(
                ChooseFileActivity.this,
                "文件  " + new File(path).getName()
                        + " 已被添加到选择列表", Toast.LENGTH_SHORT).show();*/
    }
    public void removeSelectedPath(String path){
        AllSelectPath.remove(path);
        Toast.makeText(
                ChooseFileActivity.this,
                "已经成功删除选中文件: " + new File(path).getName()
                , Toast.LENGTH_SHORT).show();
    }
    public boolean isListViewAtPositionisSelected(final int position){
        File file = this.CurrentFiles[position];
        try {
            String path = file.getCanonicalPath().toString();
            for(String ss : AllSelectPath){
                if(ss.compareTo(path) == 0)
                    return true;
            }
            return false;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }
    @Override
    public void onBackPressed(){
        try {
            if (CurrentParent.getCanonicalPath().equals(TOP_ROOT) == false) {
                CurrentParent = CurrentParent.getParentFile();
                CurrentFiles = CurrentParent.listFiles();
                inflatelistView(CurrentFiles);
            } else {
                finish_activity();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
