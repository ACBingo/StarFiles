package com.example.acbingo.myfinaltest10.FileChooseService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.acbingo.myfinaltest10.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PathScanActivity extends Activity {

    ListView pathList;
    ArrayList<String> allpath;
    List<Map<String, Object>> listItems = null;
    SimpleAdapter simpleAdapter = null;
    Button btnback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path_scan);
        pathList = (ListView) findViewById(R.id.listView1);
        allpath = getIntent().getStringArrayListExtra("pathlist");
        btnback = (Button) findViewById(R.id.pathscanbtnback);
        if(allpath != null){
            inflatelistView();
        }
        setlistener();
    }
    public void setlistener() {
        pathList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                allpath.remove(position);
                listItems.remove(position);
                simpleAdapter.notifyDataSetChanged();
                return true;
            }
        });
        btnback.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent 	intent = new Intent();
                intent.putStringArrayListExtra("pathlist",allpath);
                setResult(RESULT_OK,intent);
                finish();
                return ;
            }
        });
    }

    private void inflatelistView() {
        listItems = new ArrayList<Map<String, Object>>();
        for (String ss : allpath) {
            Map<String, Object> listItem = new HashMap<String, Object>();
            listItem.put("icon", R.drawable.file_icon_default);
            listItem.put("name", new File(ss).getName());
            listItems.add(listItem);
        }
        simpleAdapter = new SimpleAdapter(this, listItems, R.layout.path_scan_item,
                new String[] { "icon", "name" }, new int[] { R.id.selected_list_image,
                R.id.selected_list_title});
        pathList.setAdapter(simpleAdapter);
    }
    @Override
    public void onBackPressed(){
        Intent 	intent = new Intent();
        intent.putStringArrayListExtra("pathlist",allpath);
        setResult(RESULT_OK,intent);
        finish();
        return ;
    }
}

