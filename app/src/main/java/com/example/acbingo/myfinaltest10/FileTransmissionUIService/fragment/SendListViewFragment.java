package com.example.acbingo.myfinaltest10.FileTransmissionUIService.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.acbingo.myfinaltest10.FileManagerService.Utils;
import com.example.acbingo.myfinaltest10.R;
import com.example.acbingo.myfinaltest10.FileTransmissionUIService.AcceptFileList.AcceptBaseInfoAndService;
import com.example.acbingo.myfinaltest10.FileTransmissionUIService.AcceptFileList.AcceptInfo;
import com.example.acbingo.myfinaltest10.FileTransmissionUIService.AcceptFileList.SendListAdapter;
import com.example.acbingo.myfinaltest10.FileTransmissionUIService.delegate.AbsListViewDelegate;
import com.example.acbingo.myfinaltest10.activitys.SendFileInfoActivity;

import java.util.ArrayList;

public class SendListViewFragment extends BaseViewPagerFragment
        implements AbsListView.OnItemClickListener{
    private AbsListViewDelegate mAbsListViewDelegate = new AbsListViewDelegate();
    private static final String TAG = "Fragment1";
    private static final String EXTRA_CONTENT = "me.li2.update_replace_fragment_in_viewpager.extra_content";
    private ListView mListView;
    private SendListAdapter mListAdapter;
    private SendFileInfoActivity context;
    private ArrayList<AcceptInfo> mList = new ArrayList<>();

    public static SendListViewFragment newInstance(Context con , int index) {
        SendListViewFragment fragment = new SendListViewFragment();
        Bundle args = new Bundle();
        args.putInt(BUNDLE_FRAGMENT_INDEX, index);
        fragment.setArguments(args);
        fragment.context = ( SendFileInfoActivity)con;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        // mContent = (AcceptInfo) getArguments().getString(EXTRA_CONTENT);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach()");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach()");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.fragment_list_view, container, false);
        mListView = (ListView) view.findViewById(R.id.accept_listView);
        inflateListView();
        return view;
    }

    /*
         初始化listview的数据列表即 mlist  （listview 的 adapter里 保存有 mlist的引用，所以更新mlist之后 ，
                               只需为 adapter 的 updateview函数指定要更新的item的位置即可）
         初始化的数据从SendFileInfoActivity 里获取 AcceptBaseInfoAndService 实例
         里面包含了所有的接受信息，通过get函数获取需要的信息，即可完成初始化
    */
    private void inflateListView() {
        mList.clear();
        AcceptBaseInfoAndService myser = context.mBaseInfoService;
        int index = this.getArguments().getInt(BUNDLE_FRAGMENT_INDEX);
        int AllFileCount = myser.getFileCount();
        for (int i = 0 , j = 0; i < AllFileCount; i++ , j++) {
            if(myser.isAccepterChooseFile(index , i) == false) {
                   continue;
                   // 该用户 没有选择接受 文件 i 直接continue;
            }
            AcceptInfo te = new AcceptInfo();
            te.setName(myser.getFileNameAt(i));
            te.setSize(new Utils().sizeAddUnit(myser.getFileLength(i)));
            if(myser.isAccpterCompleteAcceptFile(index , i) == true){
                te.setStatusComplete();  // 文件已经完成了接受，设置为Complete状态
            }  else {
                te.setStatusPending();   // 一开始将状态设置为PENDING状态
            }
            te.setId(j);
            mList.add(te);
        }
        mListAdapter = new SendListAdapter(context, mList);
        mListView.setAdapter(mListAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView()");
    }

    // To update fragment in ViewPager, we should implement a public method for the fragment,
    // and do updating stuff in this method.
    public void updateContent(AcceptInfo content) {
        Log.d(TAG, "updateContent(" + content + ")");
        int itemIndex = content.getId();
        //mContent = content;
        int visiblePosition = mListView.getFirstVisiblePosition();
        //只有当要更新的view在可见的位置时才更新，不可见时，跳过不更新
        //Log.d("UPDATEzhaoshuai"," " + visiblePosition + " --> " + itemIndex);
        if (itemIndex - visiblePosition >= 0){
            //得到要更新的item的view
            View view = mListView.getChildAt(itemIndex - visiblePosition);
            //更新相应位置的数据
            mList.set(itemIndex, content);
            //调用adapter更新界面
            mListAdapter.updateView(view, itemIndex);
        }
    }

    public int getid() {
        return this.getArguments().getInt(BUNDLE_FRAGMENT_INDEX);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public boolean isViewBeingDragged(MotionEvent event){
        return mAbsListViewDelegate.isViewBeingDragged(event, mListView);
    }
}
