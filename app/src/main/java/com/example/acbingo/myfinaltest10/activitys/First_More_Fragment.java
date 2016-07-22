package com.example.acbingo.myfinaltest10.activitys;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.acbingo.myfinaltest10.BluetoothService.BlutoothMainAction.BluetoothChat;
import com.example.acbingo.myfinaltest10.FileChooseService.FileChoose;
import com.example.acbingo.myfinaltest10.FileManagerService.FileManagerMainActivity;
import com.example.acbingo.myfinaltest10.PCTransmission.PCMainActivity;
import com.example.acbingo.myfinaltest10.R;
import com.example.acbingo.myfinaltest10.WanTransmission.DownActivity;
import com.example.acbingo.myfinaltest10.WanTransmission.UpActivity;

/**
 * Created by tfuty on 2016-06-12.
 */
public class First_More_Fragment extends Fragment {
    Button btn_start_buletooth;
    Button btn_upload;
    Button btn_download;
    Button btn_configure;
    Button btn_about;
    Button btn_filemanager;
    Button btn_pc_file;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_more, container, false);
        find_view(view);
        set_listener(view);
        return view;
    }
    private void find_view(View view){
        btn_start_buletooth = (Button) view.findViewById(R.id.bluetooth_button);
        btn_upload = (Button) view.findViewById(R.id.upload_button);
        btn_download = (Button) view.findViewById(R.id.download_button);
        btn_configure = (Button) view.findViewById(R.id.setup_button);
        btn_about = (Button) view.findViewById(R.id.about_button);
        btn_filemanager = (Button) view.findViewById(R.id.file_manager);
        btn_pc_file = (Button) view.findViewById(R.id.pc_file_btn);
    }
    private void set_listener(View view){
        btn_start_buletooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    FileChoose.getFileChooseArrayList().clear();
                    Intent te = new Intent(getActivity(), BluetoothChat.class);
                    startActivity(te);
            }
        });
        btn_filemanager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent te = new Intent(getActivity(), FileManagerMainActivity.class);
                te.putExtra("path","null");
                startActivity(te);
            }
        });
        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileChoose.getFileChooseArrayList().clear();
                startActivity(new Intent(getActivity(),UpActivity.class));
            }
        });
        btn_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileChoose.getFileChooseArrayList().clear();
                startActivity(new Intent(getActivity(),DownActivity.class));
            }
        });
        btn_pc_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileChoose.getFileChooseArrayList().clear();
                startActivity(new Intent(getActivity(),PCMainActivity.class));
            }
        });
        btn_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                About_Fragment about_fragment = new About_Fragment();
                FragmentManager fragmentManager = getFragmentManager();

                FragmentTransaction transaction = fragmentManager.beginTransaction();

                transaction.hide(First_More_Fragment.this);

                /*transaction.setCustomAnimations(R.animator.fragment_slide_upward,
                        0,
                        R.animator.fragment_slide_upward,
                        0);*/

                transaction.add(R.id.framelayout,about_fragment);

                transaction.show(about_fragment);

                transaction.addToBackStack(null);
                transaction.commit();

            }
        });
    }
}
