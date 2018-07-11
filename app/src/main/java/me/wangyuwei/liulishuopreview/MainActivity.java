package me.wangyuwei.liulishuopreview;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;

public class MainActivity extends AppCompatActivity {

    /** 视频播放组件 */
    private PreviewVideoView mVideoView;
    /** 显示下面图片、文本、圆点 */
    private ViewPager mVpImage;
    /** 圆点指示器 */
    private PreviewIndicator mIndicator;
    /** 装载圆点上面内容（图片）的视图 */
    private List<View> mViewList = new ArrayList<>();
    /** 圆点上面内容（图片） */
    private int[] mImageResIds = new int[]{R.mipmap.intro_text_1, R.mipmap.intro_text_2, R.mipmap.intro_text_3};
    /** 适配圆点和上面内容 */
    private CustomPagerAdapter mAdapter;
    /** 页面的下标 */
    private int mCurrentPage = 0;
    /** RxJava订阅 */
    private Subscription mLoop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mVideoView = (PreviewVideoView) findViewById(R.id.vv_preview);
        mVpImage = (ViewPager) findViewById(R.id.vp_image);
        mIndicator = (PreviewIndicator) findViewById(R.id.indicator);

        mVideoView.setVideoURI(Uri.parse(getVideoPath()));

        for (int i = 0; i < mImageResIds.length; i++) {
            View view = LayoutInflater.from(this).inflate(R.layout.preview_item, null, false);
            ((ImageView) view.findViewById(R.id.iv_intro_text)).setImageResource(mImageResIds[i]);
            mViewList.add(view);
        }

        mAdapter = new CustomPagerAdapter(mViewList);
        mVpImage.setAdapter(mAdapter);
        mVpImage.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mCurrentPage = position;
                mIndicator.setSelected(mCurrentPage);
                startLoop();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        startLoop();

    }

    /**
     * 获取video文件的路径
     *
     * @return 路径
     */
    private String getVideoPath() {
        return "android.resource://" + this.getPackageName() + "/" + R.raw.intro_video;
    }

    /**
     * 开启轮询
     */
    private void startLoop() {
        if (null != mLoop) {
            //取消订阅
            mLoop.unsubscribe();
        }
        /**
         * 间隔执行
         *
         * 第一个参数：代表两个消息发送之间的间隔时间(轮训时间)
         * 第二个参数：轮训的次数
         * 第三参数：时间单位：(毫秒，秒，分钟) TimeUtil时间工具类
         */
        mLoop = Observable.interval(0, 6 * 1000, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        mVideoView.seekTo(mCurrentPage * 6 * 1000);
                        if (!mVideoView.isPlaying()) {
                            mVideoView.start();
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        if (null != mLoop) {
            mLoop.unsubscribe();
        }
        super.onDestroy();
    }


    /**
     * 适配器（适配圆点和上面内容）
     */
    public static class CustomPagerAdapter extends PagerAdapter {

        /** 装载圆点上面内容（图片）的视图 */
        private List<View> mViewList;

        public CustomPagerAdapter(List<View> viewList) {
            mViewList = viewList;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mViewList.get(position));
            return mViewList.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mViewList.get(position));
        }

        @Override
        public int getCount() {
            return mViewList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

}
