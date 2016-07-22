package com.example.acbingo.myfinaltest10.FileManagerService;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

public class UserOperate {
    public static final String TAG = "UserOperate";
    FileManagerMainActivity mFileManagerMainActivity;

    public UserOperate(FileManagerMainActivity fileManagerMainActivity) {
        mFileManagerMainActivity = fileManagerMainActivity;
    }

    public void send(File file) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        mFileManagerMainActivity.startActivity(intent);
    }

    public void send(ArrayList<HashMap<String, Object>> selectedItem) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("*/*");
        ArrayList<Uri> uris = new ArrayList<Uri>();
        for (HashMap<String, Object> item : selectedItem) {
            Uri u= Uri.fromFile((File) item.get(FileManager.FILE));
            uris.add(u); 
        }
        Print(uris);
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        mFileManagerMainActivity.startActivity(intent);
    }

    public void selectAll() {
        for (HashMap<String, Object> item : mFileManagerMainActivity.getListItem()) {
            item.put(FileManager.IS_CHECKED, true);
        }
        mFileManagerMainActivity.refreshListView();
    }

    public void selectNone() {
        for (HashMap<String, Object> item : mFileManagerMainActivity.getListItem()) {
            item.put(FileManager.IS_CHECKED, false);
        }
        mFileManagerMainActivity.refreshListView();
    }

    public void select(int index) {
        HashMap<String, Object> item = mFileManagerMainActivity.getListItem().get(index);
        Boolean checked = (Boolean) item.get(FileManager.IS_CHECKED);
        checked = !checked;
        if (checked)
            mFileManagerMainActivity.addRadioChecked();
        else
            mFileManagerMainActivity.delRadioChecked();
        item.put(FileManager.IS_CHECKED, checked);
        mFileManagerMainActivity.refreshListView();
    }

    public void sortListItem(ArrayList<HashMap<String, Object>> listItem) {
        Collections.sort(listItem, getComparator());
    }

    Comparator<HashMap<String, Object>> comparatorByName = new Comparator<HashMap<String, Object>>() {
        @Override
        public int compare(HashMap<String, Object> arg0,
                HashMap<String, Object> arg1) {
            File file0 = (File) arg0.get(FileManager.FILE);
            File file1 = (File) arg1.get(FileManager.FILE);
            if (file0.isDirectory() && file1.isFile()) {
                Print(file0.getAbsolutePath() + " " + file1.getAbsolutePath()
                        + " " + -1);
                return -1;
            } else if (file1.isDirectory() && file0.isFile()) {
                Print(file0.getAbsolutePath() + " " + file1.getAbsolutePath()
                        + " " + 1);
                return 1;
            } else {
                String title0 = (String) arg0.get(FileManager.TITLE);
                String title1 = (String) arg1.get(FileManager.TITLE);
                return title0.compareToIgnoreCase(title1);
            }
        }
    };
    Comparator<HashMap<String, Object>> comparatorBySize = new Comparator<HashMap<String, Object>>() {
        @Override
        public int compare(HashMap<String, Object> arg0,
                HashMap<String, Object> arg1) {
            File file0 = (File) arg0.get(FileManager.FILE);
            File file1 = (File) arg1.get(FileManager.FILE);
            if (file0.isDirectory() && file1.isFile()) {
                return -1;
            } else if (file1.isDirectory() && file0.isFile()) {
                return 1;
            } else {
                if (file0.isDirectory() && file1.isDirectory()) {
                    String title0 = (String) arg0.get(FileManager.TITLE);
                    String title1 = (String) arg1.get(FileManager.TITLE);
                    return title0.compareToIgnoreCase(title1);
                } else
                    return (int) ((Long) arg0.get(FileManager.SIZE) - (Long) arg1
                            .get(FileManager.SIZE));
            }
        }
    };
    Comparator<HashMap<String, Object>> comparatorByTime = new Comparator<HashMap<String, Object>>() {
        @Override
        public int compare(HashMap<String, Object> arg0,
                HashMap<String, Object> arg1) {
            File file0 = (File) arg0.get(FileManager.FILE);
            File file1 = (File) arg1.get(FileManager.FILE);
            if (file0.isDirectory() && file1.isFile()) {
                return -1;
            } else if (file1.isDirectory() && file0.isFile()) {
                return 1;
            } else {
                return ((Date) arg0.get(FileManager.TIME))
                        .compareTo((Date) arg1.get(FileManager.TIME));
            }
        }
    };
    Comparator<HashMap<String, Object>> comparatorByType = new Comparator<HashMap<String, Object>>() {
        @Override
        public int compare(HashMap<String, Object> arg0,
                HashMap<String, Object> arg1) {
            File file0 = (File) arg0.get(FileManager.FILE);
            File file1 = (File) arg1.get(FileManager.FILE);
            if (file0.isDirectory() && file1.isFile()) {
                return -1;
            } else if (file1.isDirectory() && file0.isFile()) {
                return 1;
            } else {
                String title0 = (String) arg0.get(FileManager.TITLE);
                String title1 = (String) arg1.get(FileManager.TITLE);
                String end0 = title0.substring(title0.lastIndexOf(".") + 1,
                        title0.length()).toLowerCase();
                String end1 = title1.substring(title1.lastIndexOf(".") + 1,
                        title1.length()).toLowerCase();
                if (end0.equals(end1)) {
                    return title0.compareToIgnoreCase(title1);
                } else {
                    return end0.compareToIgnoreCase(end1);
                }
            }
        }
    };

    // 按名称排序
    // 按文件名以字母顺序排列。
    //
    // 按大小排序
    // 按文件大小(文件占用的磁盘空间)排序。默认情况下会从最小到最大排列。
    //
    // 按类型排序
    // 按文件类型以字母顺序排列。会将同类文件归并到一起，然后按名称排序。
    //
    // 按修改日期排序
    // 按上次更改文件的日期和时间排序。默认情况下会从最旧到最新排列。
    public Comparator<HashMap<String, Object>> getComparator() {
        Comparator comparator = null;
        switch (mFileManagerMainActivity.getFileSort()) {
        case FileManagerMainActivity.MENU_SORT_NAME:
            comparator = comparatorByName;
            break;
        case FileManagerMainActivity.MENU_SORT_SIZE:
            comparator = comparatorBySize;
            break;
        case FileManagerMainActivity.MENU_SORT_TIME:
            comparator = comparatorByTime;
            break;
        case FileManagerMainActivity.MENU_SORT_TYPE:
            comparator = comparatorByType;
            break;
        }
        return comparator;
    }

    public static void Print(String TAG, Object obj) {
        Log.i(TAG, String.valueOf(obj));
    }

    public static void Print(Object obj) {
        Log.i(TAG, String.valueOf(obj));
    }

}
