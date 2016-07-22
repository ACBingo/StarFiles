package com.example.acbingo.myfinaltest10.FileManagerService;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.acbingo.myfinaltest10.R;
import com.jaeger.library.StatusBarUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class FileManagerMainActivity extends Activity implements OnClickListener,
        OnCreateContextMenuListener, OnItemClickListener {
    public static final int MENU_SELECT_ALL = 0;
    public static final int MENU_SORT = 1;
    public static final int MENU_SORT_NAME = 2;
    public static final int MENU_SORT_SIZE = 3;
    public static final int MENU_SORT_TIME = 4;
    public static final int MENU_SORT_TYPE = 5;
    public static final int MENU_NEW = 6;
    public static final int MENU_DISPLAY = 7;
    public static final int MENU_REFRESH = 8;
    public static final int MENU_COPY = 10;
    public static final int MENU_CUT = 11;
    public static final int MENU_SEND = 12;
    public static final int MENU_RENAME = 13;
    public static final int MENU_DELETE = 14;
    public static final int MENU_DETAIL = 15;

    public static final String TAG = "FileManagerment";
    ArrayList<HashMap<String, Object>> listItem;
    ArrayList<HashMap<String, Object>> selectedItem;
    ListView listView;
    ListViewLocationStack stack;
    String currentPath;
    Button btnPath;
    Button btnBack;
    Button btnDel;
    Button btnCopy;
    Button btnCut;
    Button btnSend;
    Button btnSelectAll;
    Button btnPaste;
    Button btnCancel;
    LinearLayout pasteOperateLinearLayout;
    LinearLayout fileOperateLinearLayout;
    FileManagerListViewAdapter fileManagerListViewAdapter;
    Utils utils = new Utils();

    FileManager fileManager;
    UserOperate userOperate;
    boolean menuSelectAllFlag = false;
    boolean displayAllFlag = false;
    int fileSortFlag = MENU_SORT_NAME;
    int longPressPosition = 0;
    boolean radioVisibleFlag = true;
    boolean copyFlag = true;
    int radioCheckedCount = 0;

    public boolean isCopy() {
        return copyFlag;
    }

    public void addRadioChecked() {
        radioCheckedCount++;
        if (radioCheckedCount == 1) {
            setFileOperateVisible(true);
        }
    }

    public void delRadioChecked() {
        radioCheckedCount--;
        if (radioCheckedCount == 0) {
            setFileOperateVisible(false);
        }
    }

    public boolean isRadioVisible() {
        return radioVisibleFlag;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filemanager_activity_main);

        StatusBarUtil.setTransparent(this);

        fileOperateLinearLayout = (LinearLayout) findViewById(R.id.fileOperateLinearLayout);
        pasteOperateLinearLayout = (LinearLayout) findViewById(R.id.pasteOperateLinearLayout);
        stack = new ListViewLocationStack(100);
        listView = (ListView) findViewById(R.id.listview);
        btnPath = (Button) findViewById(R.id.path);
        btnPath.setOnClickListener(this);
        btnBack = (Button) findViewById(R.id.back);
        btnBack.setOnClickListener(this);
        btnDel = (Button) findViewById(R.id.del);
        btnDel.setOnClickListener(this);
        btnCopy = (Button) findViewById(R.id.copy);
        btnCopy.setOnClickListener(this);
        btnCut = (Button) findViewById(R.id.cut);
        btnCut.setOnClickListener(this);
        btnSend = (Button) findViewById(R.id.send);
        btnSend.setOnClickListener(this);
        btnSelectAll = (Button) findViewById(R.id.selectAll);
        btnSelectAll.setOnClickListener(this);
        btnPaste = (Button) findViewById(R.id.paste);
        btnPaste.setOnClickListener(this);
        btnCancel = (Button) findViewById(R.id.cancel);
        btnCancel.setOnClickListener(this);
        listView.setOnItemClickListener(this);
        listView.setOnCreateContextMenuListener(this);
        fileManager = new FileManager(this);
        selectedItem = new ArrayList<HashMap<String, Object>>();
        setFileOperateVisible(false);
        setPasteOperateVisible(false);

        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            File file = Environment.getExternalStorageDirectory();
            currentPath = file.getPath();
            String comapath = getIntent().getStringExtra("path");
            //if(comapath.charAt(comapath.length() - 1) == '/')
            //  comapath = comapath.substring(0,comapath.lastIndexOf('/'));
            //Log.d("zhaoshuai" ,comapath + "****");
            if(comapath.compareTo("null") != 0) {
                currentPath = comapath;
            }
            btnPath.setText(currentPath);
            listItem = fileManager.getFilesList(currentPath);
            userOperate = new UserOperate(this);
            userOperate.sortListItem(listItem);
            fileManagerListViewAdapter = new FileManagerListViewAdapter(this, userOperate);
            listView.setAdapter(fileManagerListViewAdapter);
        } else {
            Print("No SdCard");
            finish();
        }
    }

    public void PrintList() {
        int i = 0;
        for (HashMap<String, Object> item : listItem) {
            Print(i++ + "\t" + item.get(FileManager.TITLE));
        }
    }

    public ArrayList<HashMap<String, Object>> getListItem() {
        return listItem;
    }

    public void setPasteOperateVisible(boolean visible) {
        if (visible)
            pasteOperateLinearLayout.setVisibility(View.VISIBLE);
        else
            pasteOperateLinearLayout.setVisibility(View.GONE);
    }

    public void setFileOperateVisible(boolean visible) {
        if (visible)
            fileOperateLinearLayout.setVisibility(View.VISIBLE);
        else
            fileOperateLinearLayout.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        if (view == btnPath) {
            refreshDirFromSDCard(currentPath);
        } else if (view == btnBack) {
            BackPressed();
        } else if (view == btnDel) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setIcon(android.R.drawable.ic_delete);
            dialog.setMessage(R.string.delete_info);
            dialog.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            separateSelectedItemFromListItem();
                            fileManager.delete(selectedItem);
                            int location = listView.getFirstVisiblePosition();
                            setPasteOperateVisible(false);
                            setFileOperateVisible(false);
                            refreshDirFromSDCard(currentPath);
                            listView.setSelection(location);
                            fileManager.delete((File) listItem.get(
                                    longPressPosition).get(FileManager.FILE));
                        }
                    });
            dialog.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            dialog.show();
        } else if (view == btnCopy) {
            copy();
        } else if (view == btnCut) {
            cut();
        } else if (view == btnSend) {
            separateSelectedItemFromListItem();
            userOperate.send(selectedItem);
        } else if (view == btnSelectAll) {
            Button button = (Button) view;
            if (button.getText().equals(getString(R.string.select_all))) {
                selectAll();
                button.setText(getString(R.string.select_none));
                refreshListView();
            } else {
                selectNone();
                button.setText(getString(R.string.select_all));
                refreshListView();
            }
        } else if (view == btnPaste) {
            if (copyFlag) {
                fileManager.copy(selectedItem, currentPath);
                Print(currentPath);
            } else
                fileManager.cut(selectedItem, currentPath);
            refreshDirFromSDCard(currentPath);
            setPasteOperateVisible(false);
            radioVisibleFlag = true;
        } else if (view == btnCancel) {
            setFileOperateVisible(false);
            setPasteOperateVisible(false);
            radioVisibleFlag = true;
            int location = listView.getFirstVisiblePosition();
            mergeSelectedItemToListItem();
            selectNone();
            refreshListView();
            listView.setSelection(location);
        }

    }

    public void selectAll() {
        for (HashMap<String, Object> item : listItem) {
            item.put(FileManager.IS_CHECKED, true);
        }
        radioCheckedCount = 0;
    }

    public void selectNone() {
        for (HashMap<String, Object> item : listItem) {
            item.put(FileManager.IS_CHECKED, false);
        }
        radioCheckedCount = listItem.size();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        File file = (File) listItem.get(position).get(FileManager.FILE);
        if (file.isDirectory()) {
            if(file.length() == 0){
                   Toast.makeText(getApplicationContext(),"空的文件夹" , Toast.LENGTH_SHORT);
                   return ;
            }
            stack.push(listView.getFirstVisiblePosition());
            currentPath = file.getAbsolutePath();
            btnPath.setText(currentPath);
            refreshDirFromSDCard(currentPath);
            radioCheckedCount = 0;
            setFileOperateVisible(false);
        } else {
            startActivity(fileManager.openFile(file.getPath()));
        }
    }

    public void separateSelectedItemFromListItem() {
        selectedItem.clear();
        for (HashMap<String, Object> item : listItem) {
            if ((Boolean) item.get(FileManager.IS_CHECKED))
                selectedItem.add(item);
        }
        for (HashMap<String, Object> item : selectedItem) {
            listItem.remove(item);
        }
    }

    public void mergeSelectedItemToListItem() {
        listItem.addAll(selectedItem);
        selectedItem.clear();
        userOperate.sortListItem(listItem);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        if (radioCheckedCount == 0) {
            menu.add(Menu.NONE, MENU_COPY, 0, R.string.copy);
            menu.add(Menu.NONE, MENU_CUT, 1, R.string.cut);
            menu.add(Menu.NONE, MENU_SEND, 2, R.string.send);
            menu.add(Menu.NONE, MENU_RENAME, 3, R.string.rename);
            menu.add(Menu.NONE, MENU_DELETE, 4, R.string.delete);
            menu.add(Menu.NONE, MENU_DETAIL, 5, R.string.detail);
            longPressPosition = ((AdapterContextMenuInfo) menuInfo).position;
        }
    }

    public void refreshListView() {
        fileManagerListViewAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*
		 *
		 * add()方法的四个参数，依次是：
		 *
		 * 1、组别，如果不分组的话就写Menu.NONE,
		 *
		 * 2、Id，这个很重要，Android根据这个Id来确定不同的菜单
		 *
		 * 3、顺序，那个菜单现在在前面由这个参数的大小决定
		 *
		 * 4、文本，菜单的显示文本
		 */
        // setIcon()方法为菜单设置图标，这里使用的是系统自带的图标，同学们留意一下,以
        // android.R开头的资源是系统提供的，我们自己提供的资源是以R开头的
        menu.add(Menu.NONE, MENU_SELECT_ALL, 1, R.string.menu_select_all);
        SubMenu sortSubMenu = menu.addSubMenu(Menu.NONE, MENU_SORT, 2,
                R.string.menu_sort);
        int sortSubMenuGroup = 1;
        sortSubMenu.add(sortSubMenuGroup, MENU_SORT_NAME, Menu.NONE,
                R.string.menu_sort_name).setChecked(true);
        sortSubMenu.add(sortSubMenuGroup, MENU_SORT_SIZE, Menu.NONE,
                R.string.menu_sort_size);
        sortSubMenu.add(sortSubMenuGroup, MENU_SORT_TIME, Menu.NONE,
                R.string.menu_sort_time);
        sortSubMenu.add(sortSubMenuGroup, MENU_SORT_TYPE, Menu.NONE,
                R.string.menu_sort_type);
        sortSubMenu.setGroupCheckable(sortSubMenuGroup, true, true);
        // menu.add(Menu.NONE, MENU_NEW, 3,
        // R.string.menu_new).setIcon(android.R.drawable.ic_menu_add);
        menu.add(Menu.NONE, MENU_NEW, 3, R.string.menu_new);
        menu.add(Menu.NONE, MENU_DISPLAY, 4, R.string.menu_diaplay_hiden);
        menu.add(Menu.NONE, MENU_REFRESH, 5, R.string.menu_refresh);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_SELECT_ALL:
                menuSelectAllFlag = !menuSelectAllFlag;
                if (menuSelectAllFlag) {
                    item.setTitle(R.string.menu_select_all);
                    userOperate.selectAll();
                } else {
                    item.setTitle(R.string.menu_cancel_select_all);
                    userOperate.selectNone();
                }
                break;
            case MENU_SORT:
                break;
            case MENU_SORT_NAME:
                item.setChecked(true);
                fileSortFlag = MENU_SORT_NAME;
                userOperate.sortListItem(listItem);
                refreshListView();
                break;
            case MENU_SORT_SIZE:
                item.setChecked(true);
                fileSortFlag = MENU_SORT_SIZE;
                userOperate.sortListItem(listItem);
                refreshListView();
                break;
            case MENU_SORT_TIME:
                item.setChecked(true);
                fileSortFlag = MENU_SORT_TIME;
                userOperate.sortListItem(listItem);
                refreshListView();
                break;
            case MENU_SORT_TYPE:
                item.setChecked(true);
                fileSortFlag = MENU_SORT_TYPE;
                userOperate.sortListItem(listItem);
                refreshListView();
                break;
            case MENU_NEW:
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle(R.string.menu_new);
                dialog.setMessage(R.string.input_new_folder_info);
                final EditText input = new EditText(this);
                dialog.setView(input);
                dialog.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Print("NewFloder");
                                String inputStr = input.getText().toString().trim();
                                String errorStr = null;
                                if (inputStr.equals(""))
                                    errorStr = getResources().getString(
                                            R.string.new_folder_null_err);
                                else if (inputStr.contains(File.separator))
                                    errorStr = getResources().getString(
                                            R.string.new_folder_spec_char_err);
                                else if (fileManager.newFolder(currentPath
                                        + File.separator + inputStr) == false)
                                    errorStr = getResources().getString(
                                            R.string.new_folder_exist_err);
                                else
                                    refreshDirFromSDCard(currentPath);
                                if (errorStr != null) {
                                    AlertDialog.Builder warningDialog = new AlertDialog.Builder(
                                            FileManagerMainActivity.this);
                                    warningDialog
                                            .setIcon(android.R.drawable.ic_dialog_alert);
                                    warningDialog.setMessage(errorStr);
                                    warningDialog.setPositiveButton("OK", null);
                                    warningDialog.show();
                                }
                            }
                        });
                dialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                dialog.show();
                break;
            case MENU_DISPLAY:
                displayAllFlag = !displayAllFlag;
                if (displayAllFlag) {
                    item.setTitle(R.string.menu_diaplay_hiden);
                } else {
                    item.setTitle(R.string.menu_not_diaplay_hiden);
                }
                refreshDirFromSDCard(currentPath);
                break;
            case MENU_REFRESH:
                refreshDirFromSDCard(currentPath);
                break;
        }
        return false;

    }

    public void refreshDirFromSDCard(String path) {
        int location = listView.getFirstVisiblePosition();
        listItem = fileManager.getFilesList(path);
        listItem.removeAll(selectedItem);
        userOperate.sortListItem(listItem);
        refreshListView();
        radioCheckedCount = 0;
        listView.setSelection(location);
    }

    public int getFileSort() {
        return fileSortFlag;
    }

    public boolean isDisplayAll() {
        return displayAllFlag;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_COPY:
                listItem.get(longPressPosition).put(FileManager.IS_CHECKED, true);
                copy();
                break;
            case MENU_CUT:
                listItem.get(longPressPosition).put(FileManager.IS_CHECKED, true);
                cut();
                break;
            case MENU_SEND:
                userOperate.send((File) listItem.get(longPressPosition).get(
                        FileManager.FILE));
                break;
            case MENU_RENAME: {
                final File file = (File) listItem.get(longPressPosition).get(
                        FileManager.FILE);
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setIcon(android.R.drawable.ic_dialog_info);
                dialog.setTitle(R.string.rename);
                dialog.setMessage(R.string.input_rename_info);
                final EditText input = new EditText(this);
                dialog.setView(input);
                dialog.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Print("Rename");
                                String inputStr = input.getText().toString().trim();
                                String errorStr = null;
                                if (inputStr.equals(""))
                                    errorStr = getResources().getString(
                                            R.string.rename_null_err);
                                else if (inputStr.contains(File.separator))
                                    errorStr = getResources().getString(
                                            R.string.rename_spec_char_err);
                                else if (fileManager.rename(file.getAbsolutePath(),
                                        currentPath + File.separator + inputStr) == false)
                                    errorStr = getResources().getString(
                                            R.string.rename_exist_err);
                                else
                                    refreshDirFromSDCard(currentPath);
                                if (errorStr != null) {
                                    AlertDialog.Builder warningDialog = new AlertDialog.Builder(
                                            FileManagerMainActivity.this);
                                    warningDialog
                                            .setIcon(android.R.drawable.ic_dialog_alert);
                                    warningDialog.setMessage(errorStr);
                                    warningDialog.setPositiveButton("OK", null);
                                    warningDialog.show();
                                }
                            }
                        });
                dialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                dialog.show();
            }
            break;
            case MENU_DELETE: {
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setIcon(android.R.drawable.ic_menu_delete);
                dialog.setMessage(R.string.delete_info);
                dialog.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                fileManager.delete((File) listItem.get(
                                        longPressPosition).get(FileManager.FILE));
                                refreshDirFromSDCard(currentPath);
                            }
                        });
                dialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                dialog.show();
            }
            break;
            case MENU_DETAIL:
                File file = (File) listItem.get(longPressPosition).get(
                        FileManager.FILE);
                AlertDialog.Builder detailDialog = new AlertDialog.Builder(
                        FileManagerMainActivity.this);
                detailDialog.setIcon(android.R.drawable.ic_dialog_info);
                String path = getResources().getString(R.string.detail_path)
                        + file.getAbsolutePath();
                String size = getResources().getString(R.string.detail_size)
                        + utils.sizeAddUnit(fileManager.fileSize(file));
                String canRead = getResources().getString(R.string.detail_can_read)
                        + file.canRead();
                String canWrite = getResources().getString(
                        R.string.detail_can_write)
                        + file.canWrite();
                String hidden = getResources().getString(R.string.detail_hidden)
                        + file.isHidden();
                detailDialog.setTitle(file.getName());
                detailDialog.setItems(new String[]{path, size, canRead, canWrite,
                        hidden}, null);
                detailDialog.setPositiveButton("OK", null);
                detailDialog.show();
                break;
        }

        return super.onContextItemSelected(item);
    }

    private void commonInCopyAndCut() {
        separateSelectedItemFromListItem();
        radioVisibleFlag = false;
        int location = listView.getFirstVisiblePosition();
        setPasteOperateVisible(true);
        setFileOperateVisible(false);
        refreshListView();
        listView.setSelection(location);
    }

    public void copy() {
        copyFlag = true;
        commonInCopyAndCut();
    }

    public void cut() {
        copyFlag = false;
        commonInCopyAndCut();
    }

    @Override
    public void onBackPressed() {
        int numCount = 0;
        for (int i = 0; i < currentPath.length(); i++)
            if (currentPath.charAt(i) == '/') numCount++;
        if (numCount <= 1){
               finish();
               return ;
        }
        BackPressed();
    }

    public void BackPressed() {
        int numCount = 0;
        for (int i = 0; i < currentPath.length(); i++)
            if (currentPath.charAt(i) == '/') numCount++;
        if (numCount <= 1) {
            Toast.makeText(this, this.getResources().getString(R.string.alread_in_root_path),
                    Toast.LENGTH_SHORT).show();
            // Print("my ------> here ");
            return;  //只有一个/认为回到根路径，不在后退
        }
        currentPath = currentPath
                .substring(0, currentPath.lastIndexOf("/"));
        radioCheckedCount = 0;
        setFileOperateVisible(false);
        btnPath.setText(currentPath);
        refreshDirFromSDCard(currentPath);
        listView.setSelection(stack.pull());
        Print(currentPath);
    }

    public static void Print(String TAG, Object obj) {
        Log.i(TAG, String.valueOf(obj));
    }

    public static void Print(Object obj) {
        Log.i(TAG, String.valueOf(obj));
    }

}
