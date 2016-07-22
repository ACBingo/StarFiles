package com.example.acbingo.myfinaltest10.activitys;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.TextView;

import com.example.acbingo.myfinaltest10.FileManagerService.Utils;
import com.example.acbingo.myfinaltest10.R;
import com.example.acbingo.myfinaltest10.FileTransmissionUIService.AcceptFileList.AcceptBaseInfoAndService;
import com.example.acbingo.myfinaltest10.FileTransmissionUIService.AcceptFileList.AcceptInfo;
import com.example.acbingo.myfinaltest10.FileTransmissionUIService.AcceptFileList.AcceptThread;
import com.example.acbingo.myfinaltest10.FileTransmissionUIService.fragment.ReceiveListViewFragment;
import com.example.acbingo.myfinaltest10.FileTransmissionUIService.tools.ScrollableFragmentListener;
import com.example.acbingo.myfinaltest10.FileTransmissionUIService.tools.ScrollableListener;
import com.example.acbingo.myfinaltest10.FileTransmissionUIService.tools.ViewPagerHeaderHelper;
import com.example.acbingo.myfinaltest10.FileTransmissionUIService.widget.SlidingTabLayout;
import com.example.acbingo.myfinaltest10.FileTransmissionUIService.widget.TouchCallbackLayout;
import com.jaeger.library.StatusBarUtil;

import java.util.ArrayList;

import me.itangqi.waveloadingview.WaveLoadingView;

public class ReceiveFileInfoActivity extends ActionBarActivity
        implements TouchCallbackLayout.TouchEventListener, ScrollableFragmentListener,
        ViewPagerHeaderHelper.OnViewPagerTouchListener {

    private static final long DEFAULT_DURATION = 300L;
    private static final float DEFAULT_DAMPING = 1.5f;
    public static final int BEGIN = 111;
    public static final int UPDATE = 222;
    public static final int END = 444;
    public static final String UPDATE_ACCEPT_LEN = "ACCEPTLEN";
    public static final String CHOICE_LIST = "receivechoicelist";
    public static final int RECEIVE_CHOICE_LIST  = 55;
    public static final int UPDATETOTAL =25;
    public static final int ALLEND =344;

    private SparseArrayCompat<ScrollableListener> mScrollableListenerArrays =
            new SparseArrayCompat<>();
    private int mTouchSlop;
    private int mTabHeight;
    private int mHeaderHeight;
    private Interpolator mInterpolator = new DecelerateInterpolator();


    public static final int REQUEST_UPDATAE_PAGE2 = 22;
    private static final String TAG = "Adapter";

    private ViewPagerHeaderHelper mViewPagerHeaderHelper;
    private int PAGE_COUNT = 5;
    private ViewPager mViewPager;
    private View mHeaderLayoutView;
    public Handler mhandler = new Handler();
    public AcceptBaseInfoAndService mBaseInfoService;
    private FragmentPagerAdapter mViewPagerAdapter;

    //用来存储每个页面需要更新的信息，一次只能更新每个page里面的一项内容
    private ArrayList<AcceptInfo> mContent;
    private WaveLoadingView myWaveloadingView;
    private ReceiveFileService tmp = new ReceiveFileService(this);
    private TextView HintText ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_file_client);

        StatusBarUtil.setTransparent(this);

        myWaveloadingView = (WaveLoadingView) findViewById(R.id.waveLoadingView);
        // set_handler();

        mContent = new ArrayList<>();
        for (int i = 0; i < PAGE_COUNT; i++)
            mContent.add(new AcceptInfo());
        init_set_base_info();
        //状态栏透明
        StatusBarUtil.setTransparent(this);
        mTouchSlop = ViewConfiguration.get(this).getScaledTouchSlop();
        mTabHeight = 0;
        mHeaderHeight = 0;
        mViewPagerHeaderHelper = new ViewPagerHeaderHelper(this, this);
        /*TouchCallbackLayout touchCallbackLayout = (TouchCallbackLayout) findViewById(R.id.layout);
        touchCallbackLayout.setTouchEventListener(this);*/
        mHeaderLayoutView = findViewById(R.id.header);
        SlidingTabLayout slidingTabLayout = (SlidingTabLayout) findViewById(R.id.tabs);
        //slidingTabLayout.setDistributeEvenly(true);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(mViewPagerAdapter);
        slidingTabLayout.setViewPager(mViewPager);
        ViewCompat.setTranslationY(mViewPager, mHeaderHeight);
        //set_test_thread();
    }
    /*
       设置这个函数的原因是要停顿一下，当接受到Re
    */
    public void init_set_base_info(){
        HintText = (TextView) findViewById(R.id.HintText);
        HintText.setText("正在等待接收哦");
        HintText.setVisibility(View.VISIBLE);
        boolean[][]  maze = new boolean[1][100];
        int FileCount = tmp.getFileCount();
        PAGE_COUNT = 0;
        long[] filelen = new long[100];
        ArrayList<String> filename = new ArrayList<>();
        this.mBaseInfoService = new AcceptBaseInfoAndService(
                filename, filelen, maze, this.PAGE_COUNT
        );   //这里只是虚拟初始化，因为当前需要显示的页面为0个

        set_myPagerAdapter();
        set_handler();
        tmp.setHandler(this.mhandler);
    }
    public void set_base_info() {
        HintText.setVisibility(View.INVISIBLE);
        boolean[][]  maze = new boolean[1][100]; // 第一维度设置为1因为接受者只有用户本身
        int FileCount = tmp.getFileCount();
        PAGE_COUNT = 1;

        for(int i = 0 ; i< FileCount ; i++){
               maze[0][i] = tmp.getFileChoose(i);
        }

        long[] filelen = new long[100];
        for(int i = 0 ; i < FileCount ; i++){
            filelen[i] = tmp.getFileSize(i);
        }

        ArrayList<String> filename = new ArrayList<>();
        for(int i = 0 ; i < FileCount ; i++){
            filename.add(tmp.getFileName(i));
        }
        Log.d("zhaoshuai","" + filelen + " "+ filename.get(0));

        this.mBaseInfoService = new AcceptBaseInfoAndService(
                filename, filelen, maze, this.PAGE_COUNT
        );
        //Log.d("hehe--->" , "" + PAGE_COUNT  + " " + FileCount + " " + filename.get(0));
        set_myPagerAdapter();  // 这里重新设置一下adapter使之变成
        SlidingTabLayout slidingTabLayout = (SlidingTabLayout) findViewById(R.id.tabs);
        mViewPager.setAdapter(mViewPagerAdapter);
        slidingTabLayout.setViewPager(mViewPager);
        ViewCompat.setTranslationY(mViewPager, mHeaderHeight);
        // 在这里为设置每个用户的名字
    }

    /*
       处理发送来的文件接受信息
       BEGIN : 文件开始接受，接受到该命令之后对应pager下的对应的listview的对应item由就绪状态切换到Start
       UPDATE ：对应listview下对应item已经处于进度显示状态，update命令携带了更新进度的信息，
               arg1 ：需要更新fragment编号
               arg2 ：需要更新的对应fragment第几个文件需要更新，这个并不是实际编号，因为有些文件是被接受方
                      拒绝接受的，所以应该先按照 接受参照表  找到这个文件在listview中对应的item编号
               接受长度 ： 代表了上面所述的这个文件已经接受了多长通过查找 文件长度参照表 来确定具体接受的比例
               注意：这里应该也需要实现对接受总进度的更新
       END ： arg1 , arg2 如上所述，选定具体文件，代表该文件的传输完成,对应的item由进度显示切换到 complete状态
    */
    public void set_handler() {
        mhandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ReceiveFileService.SERVICE_IS_READY:
                         //Log.d("zhaoshuai","hehe -->> ");
                         set_base_info();
                         break;
                    case ReceiveFileService.RECEVIE_TABLE:
                         ArrayList<String> namelist = msg.getData().getStringArrayList(
                                 ReceiveFileService.FILELIST
                         );
                         showDialog5(namelist);
                         break;
                    case UPDATETOTAL :
                          int val = (int)msg.obj;
                          myWaveloadingView.setProgressValue(val);
                          myWaveloadingView.setCenterTitle(
                                String.valueOf(val)+" %"
                          );
                          break;
                    case RECEIVE_CHOICE_LIST :
                          Message mymsg = new Message();
                          mymsg.what = ReceiveFileService.STRAT_RECEIVE;
                          mymsg.setData(msg.getData());
                          tmp.getHandler().sendMessage(mymsg);
                          break;
                    case UPDATE:
                        int AccepterId = 0;
                        int FileId = msg.arg2;
                        if(mBaseInfoService.isAccpterCompleteAcceptFile(AccepterId,FileId)){
                            break;   //当文件已接收到END信号又收到更新信息则取消这次更新。
                        }

                        int ItemId = mBaseInfoService.getFileShowPositionByAccepterIdAndFileId(AccepterId, FileId);
                        int percent =(int) msg.obj;
                        if(percent >= 100) {
                            percent = 99;  //在未接到END信号之前percent最大值为99
                        }
                        AcceptInfo te = new AcceptInfo();
                        te.setPage_id(AccepterId);
                        te.setId(ItemId);
                        te.setDownloadPercent(String.valueOf(percent));
                        te.setName(mBaseInfoService.getFileNameAt(FileId));
                        te.setSize(new Utils().sizeAddUnit(mBaseInfoService.getFileLength(FileId)));
                        mContent.set(AccepterId, te);
                        //Log.d("UPDATEzhaoshuai", " ----> " + AccepterId + " " + FileId + " " + te.getName() + " "+ te.getDownloadPercent());
                        notifyViewPagerDataSetChanged();
                        break;
                    case BEGIN:
                        AccepterId = 0;
                        FileId = msg.arg2;
                        ItemId = mBaseInfoService.getFileShowPositionByAccepterIdAndFileId(AccepterId, FileId);
                        te = new AcceptInfo();
                        te.setPage_id(AccepterId);
                        te.setId(ItemId);
                        te.setStatusStart();  // 状态设置为Start
                        te.setSize(new Utils().sizeAddUnit(mBaseInfoService.getFileLength(FileId)));
                        te.setName(mBaseInfoService.getFileNameAt(FileId));
                        mContent.set(AccepterId, te);
                        notifyViewPagerDataSetChanged();
                        break;
                    case END:
                        AccepterId = 0;
                        FileId = msg.arg2;
                        ItemId = mBaseInfoService.getFileShowPositionByAccepterIdAndFileId(AccepterId, FileId);
                        te = new AcceptInfo();
                        te.setPage_id(AccepterId);
                        te.setId(ItemId);
                        te.setStatusComplete();  // 状态设置为接受完成
                        te.setName(mBaseInfoService.getFileNameAt(FileId));
                        te.setSize(new Utils().sizeAddUnit(mBaseInfoService.getFileLength(FileId)));
                        mContent.set(AccepterId, te);
                        notifyViewPagerDataSetChanged();
                        mBaseInfoService.setFileTransCompletedByAccepterIdAndFileId(AccepterId , FileId);
                        //Log.d("UPDATEzhaoshuai", " ----> "  +  AccepterId + " "  + FileId + " " + "END");
                        //  这里需要额外设置一下文件接收完成矩阵，为了完成的item在新一次被刷新后仍然显示完成状态
                        //  这就需要这个矩阵来做参考
                        break;
                    case ALLEND:
                        Intent tet = new Intent(getApplicationContext() , ReceiveFileEndActivity.class);
                        startActivity(tet);
                        ReceiveFileInfoActivity.this.finish();
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    /*
         设置  测试 thread
         需要为测试thread 提供本 Activity 的 handler 和 要更新的页面的编号
         要更新的item 和 要更新的速度都有thread决定
    */
    public void set_test_thread() {
        AcceptThread te = new AcceptThread(mhandler, 3);
        te.start();
    }

    @Override
    public boolean onLayoutInterceptTouchEvent(MotionEvent event) {

        return mViewPagerHeaderHelper.onLayoutInterceptTouchEvent(event,
                mTabHeight + mHeaderHeight);
    }

    @Override
    public boolean onLayoutTouchEvent(MotionEvent event) {
        return mViewPagerHeaderHelper.onLayoutTouchEvent(event);
    }

    @Override
    public boolean isViewBeingDragged(MotionEvent event) {
        return mScrollableListenerArrays.valueAt(mViewPager.getCurrentItem())
                .isViewBeingDragged(event);
    }

    @Override
    public void onMoveStarted(float y) {

    }

    @Override
    public void onMove(float y, float yDx) {
        float headerTranslationY = ViewCompat.getTranslationY(mHeaderLayoutView) + yDx;
        if (headerTranslationY >= 0) { // pull end
            headerExpand(0L);
        } else if (headerTranslationY <= -mHeaderHeight) { // push end
            headerFold(0L);
        } else {
            ViewCompat.animate(mHeaderLayoutView)
                    .translationY(headerTranslationY)
                    .setDuration(0)
                    .start();
            ViewCompat.animate(mViewPager)
                    .translationY(headerTranslationY + mHeaderHeight)
                    .setDuration(0)
                    .start();
        }
    }

    @Override
    public void onMoveEnded(boolean isFling, float flingVelocityY) {

        float headerY = ViewCompat.getTranslationY(mHeaderLayoutView); // 0到负数
        if (headerY == 0 || headerY == -mHeaderHeight) {
            return;
        }

        if (mViewPagerHeaderHelper.getInitialMotionY() - mViewPagerHeaderHelper.getLastMotionY()
                < -mTouchSlop) {  // pull > mTouchSlop = expand
            headerExpand(headerMoveDuration(true, headerY, isFling, flingVelocityY));
        } else if (mViewPagerHeaderHelper.getInitialMotionY()
                - mViewPagerHeaderHelper.getLastMotionY()
                > mTouchSlop) { // push > mTouchSlop = fold
            headerFold(headerMoveDuration(false, headerY, isFling, flingVelocityY));
        } else {
            if (headerY > -mHeaderHeight / 2f) {  // headerY > header/2 = expand
                headerExpand(headerMoveDuration(true, headerY, isFling, flingVelocityY));
            } else { // headerY < header/2= fold
                headerFold(headerMoveDuration(false, headerY, isFling, flingVelocityY));
            }
        }
    }

    private long headerMoveDuration(boolean isExpand, float currentHeaderY, boolean isFling,
                                    float velocityY) {

        long defaultDuration = DEFAULT_DURATION;

        if (isFling) {

            float distance = isExpand ? Math.abs(mHeaderHeight) - Math.abs(currentHeaderY)
                    : Math.abs(currentHeaderY);
            velocityY = Math.abs(velocityY) / 1000;

            defaultDuration = (long) (distance / velocityY * DEFAULT_DAMPING);

            defaultDuration = defaultDuration > DEFAULT_DURATION ? DEFAULT_DURATION : defaultDuration;
        }

        return defaultDuration;
    }

    private void headerFold(long duration) {
        ViewCompat.animate(mHeaderLayoutView)
                .translationY(-mHeaderHeight)
                .setDuration(duration)
                .setInterpolator(mInterpolator)
                .start();

        ViewCompat.animate(mViewPager).translationY(0).
                setDuration(duration).setInterpolator(mInterpolator).start();

        mViewPagerHeaderHelper.setHeaderExpand(false);
    }

    private void headerExpand(long duration) {
        ViewCompat.animate(mHeaderLayoutView)
                .translationY(0)
                .setDuration(duration)
                .setInterpolator(mInterpolator)
                .start();

        ViewCompat.animate(mViewPager)
                .translationY(mHeaderHeight)
                .setDuration(duration)
                .setInterpolator(mInterpolator)
                .start();
        mViewPagerHeaderHelper.setHeaderExpand(true);
    }

    @Override
    public void onFragmentAttached(ScrollableListener listener, int position) {
        mScrollableListenerArrays.put(position, listener);
    }

    @Override
    public void onFragmentDetached(ScrollableListener listener, int position) {
        mScrollableListenerArrays.remove(position);
    }

    void set_myPagerAdapter(){
        mViewPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return PAGE_COUNT;
            }

            // Return the Fragment associated with a specified position.
            @Override
            public Fragment getItem(int position) {
                if (position < PAGE_COUNT) {
                    return ReceiveListViewFragment.newInstance(ReceiveFileInfoActivity.this, position);
                }
                return null;
            }

            // Remove a page for the given position. The adapter is responsible for removing the view from its container.
            @Override
            public void destroyItem(android.view.ViewGroup container, int position, Object object) {
                super.destroyItem(container, position, object);
            }


            @Override
            // To update fragment in ViewPager, we should override getItemPosition() method,
            // in this method, we call the fragment's public updating method.
            public int getItemPosition(Object object) {
               ReceiveListViewFragment te = (ReceiveListViewFragment) object;
                te.updateContent(mContent.get(te.getid()));
                return super.getItemPosition(object);
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mBaseInfoService.getAccepterNameAt(position);
            }
        };
    }


    private ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {

            notifyViewPagerDataSetChanged();
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    /**
     * To update fragment in ViewPager, we should call PagerAdapter.notifyDataSetChanged() when data changed.
     * we should also override FragmentPagerAdapter.getItemPosition(), and
     * implement a public method for updating fragment.
     * Refer to [Update Fragment from ViewPager](http://stackoverflow.com/a/18088509/2722270)
     */
    private void notifyViewPagerDataSetChanged() {

        mViewPagerAdapter.notifyDataSetChanged();
    }
    public void showDialog5(ArrayList<String> list){
        //定义复选框选项
        final String[] multiChoiceItems = new String[list.size()];
        for(int i = 0 ; i < list.size() ; i++)
                multiChoiceItems[i] = list.get(i);
        //复选框默认值：false=未选;true=选中 ,各自对应items[i]
        final boolean[] defaultSelectedStatus = new boolean[multiChoiceItems.length];
        for(int i = 0 ; i < defaultSelectedStatus.length ; i++)
                defaultSelectedStatus[i] = false;
        final Boolean[] result = new Boolean[multiChoiceItems.length];

        final StringBuilder sb = new StringBuilder();
        //创建对话框
        new AlertDialog.Builder(ReceiveFileInfoActivity.this)
                .setTitle("请选择要接受的文件")//设置对话框标题
                .setMultiChoiceItems(multiChoiceItems, defaultSelectedStatus, new DialogInterface.OnMultiChoiceClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which,
                                        boolean isChecked) {
                        //来回重复选择取消，得相应去改变item对应的bool值，点击确定时，根据这个bool[],得到选择的内容
                        defaultSelectedStatus[which] = isChecked;
                    }
                })  //设置对话框[肯定]按钮
                .setPositiveButton("确定",new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Message msg = new Message();
                        msg.what = RECEIVE_CHOICE_LIST;
                        Bundle bun = new Bundle();

                        bun.putBooleanArray(CHOICE_LIST,defaultSelectedStatus);
                        msg.setData(bun);

                        mhandler.sendMessage(msg);

                        int j = 0 ;
                        for(boolean x : defaultSelectedStatus){

                        }
                        // TODO Auto-generated method stub
                        //Toast.makeText(ReceiveFileInfoActivity.this,sb.toString(), Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("取消", null)//设置对话框[否定]按钮
                .show();
    }
    @Override
    public void onBackPressed() {
        // super.onBackPressed();
    }
}
