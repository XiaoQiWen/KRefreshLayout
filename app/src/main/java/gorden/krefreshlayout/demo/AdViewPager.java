package gorden.krefreshlayout.demo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * 广告轮播ViewPager
 */
@SuppressWarnings("unused")
public class AdViewPager extends RelativeLayout {
    private static final int RMP = LayoutParams.MATCH_PARENT;
    private static final int RWP = LayoutParams.WRAP_CONTENT;
    private static final int LWC = LinearLayout.LayoutParams.WRAP_CONTENT;
    private static final int WAIT_AUTO_PLAY = 1000;

    //Point位置
    public static final int CENTER = 0;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;

    private RelativeLayout mPointContainerRl;
    private LinearLayout mPointRealContainerLl;

    private ViewPager mViewPager;
    //本地图片资源
    private List<Integer> mImages;
    //网络图片资源
    private List<String> mImageUrls;
    //是否是网络图片
    private boolean mIsImageUrl = false;
    //是否只有一张图片
    private boolean mIsOneImg = false;
    //是否可以自动播放
    private boolean mAutoPlayAble = true;
    //是否正在播放
    private boolean mIsAutoPlaying = false;
    //自动播放时间
    private int mAutoPalyTime = 5000;

    //当前页面位置
    private int mCurrentPositon;
    //指示点位置
    private int mPointPosition = CENTER;
    //指示点资源
    private int mPointDrawableResId = R.drawable.selector_adpager_point;
    //指示容器背景
    private Drawable mPointContainerBackgroundDrawable;
    //指示容器布局规则
    private LayoutParams mPointRealContainerLp;

    //指示点是否可见
    private boolean mPointsIsVisible = true;

    /**
     * 自动轮播handler
     */
    private Handler mAutoPlayHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            mCurrentPositon++;
            mViewPager.setCurrentItem(mCurrentPositon);
            mAutoPlayHandler.sendEmptyMessageDelayed(WAIT_AUTO_PLAY, mAutoPalyTime);
            return false;
        }
    });

    public AdViewPager(Context context) {
        this(context, null);
    }

    public AdViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AdViewPager);

        mPointsIsVisible = a.getBoolean(R.styleable.AdViewPager_ad_points_visibility, true);
        mPointPosition = a.getInt(R.styleable.AdViewPager_ad_points_position, CENTER);
        mPointContainerBackgroundDrawable
                = a.getDrawable(R.styleable.AdViewPager_ad_points_container_background);

        a.recycle();

        setLayout(context);
    }

    private void setLayout(Context context) {
        //关闭view的OverScroll
        setOverScrollMode(OVER_SCROLL_NEVER);
        //设置指示器背景
        if (mPointContainerBackgroundDrawable == null) {
            mPointContainerBackgroundDrawable = new ColorDrawable(Color.parseColor("#33AAAAAA"));
        }
        //添加ViewPager
        mViewPager = new ViewPager(context);
        addView(mViewPager, new LayoutParams(RMP, RMP));
        //设置指示器背景容器
        mPointContainerRl = new RelativeLayout(context);
        if (Build.VERSION.SDK_INT >= 16) {
            mPointContainerRl.setBackground(mPointContainerBackgroundDrawable);
        } else {
            mPointContainerRl.setBackgroundDrawable(mPointContainerBackgroundDrawable);
        }
        //设置内边距
        mPointContainerRl.setPadding(0, 10, 0, 10);
        //设定指示器容器布局及位置
        LayoutParams pointContainerLp = new LayoutParams(RMP, RWP);
        pointContainerLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        addView(mPointContainerRl, pointContainerLp);
        //设置指示器容器
        mPointRealContainerLl = new LinearLayout(context);
        mPointRealContainerLl.setOrientation(LinearLayout.HORIZONTAL);
        mPointRealContainerLp = new LayoutParams(RWP, RWP);
        mPointContainerRl.addView(mPointRealContainerLl, mPointRealContainerLp);
        mPointContainerRl.setVisibility(GONE);
        //设置指示器布局位置
        if (mPointPosition == CENTER) {
            mPointRealContainerLp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        } else if (mPointPosition == LEFT) {
            mPointRealContainerLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        } else if (mPointPosition == RIGHT) {
            mPointRealContainerLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        }
    }

    /**
     * 设置本地图片
     */
    public void setImages(List<Integer> images) {
        //加载本地图片
        mIsImageUrl = false;
        mIsOneImg = false;

        this.mImages = images;
        if (images.size() <= 1)
            mIsOneImg = true;

        //初始化ViewPager
        initViewPager();
    }

    /**
     * 设置网络图片
     */
    public void setImagesUrl(List<String> urls) {
        //加载网络图片
        mIsImageUrl = true;
        mIsOneImg = false;

        this.mImageUrls = urls;
        if (urls.size() <= 1)
            mIsOneImg = true;
        //初始化ViewPager
        initViewPager();
    }

    /**
     * 设置指示点是否可见
     */
    public void setPointsIsVisible(boolean isVisible) {
        if (mPointContainerRl != null) {
            if (isVisible) {
                mPointContainerRl.setVisibility(View.VISIBLE);
            } else {
                mPointContainerRl.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 对应三个位置 CENTER,RIGHT,LEFT
     */
    public void setPoinstPosition(int position) {
        //设置指示器布局位置
        if (position == CENTER) {
            mPointRealContainerLp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        } else if (position == LEFT) {
            mPointRealContainerLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        } else if (position == RIGHT) {
            mPointRealContainerLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        }
    }

    private void initViewPager() {
        //当图片多于1张时添加指示点
        mPointRealContainerLl.removeAllViews();
        mPointContainerRl.setVisibility(GONE);
        if (!mIsOneImg) {
            addPoints();
        }
        //设置ViewPager
        AdPageAdapter adapter = new AdPageAdapter();
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(mOnPageChangeListener);
        //跳转到首页
        mViewPager.setCurrentItem(1, false);
        //当图片多于1张时开始轮播
        if (!mIsOneImg) {
            startAutoPlay();
        } else {
            stopAutoPlay();
        }
    }

    /**
     * 返回真实的位置
     */
    private int toRealPosition(int position) {
        int realPosition;
        if (mIsImageUrl) {
            realPosition = (position - 1) % mImageUrls.size();
            if (realPosition < 0)
                realPosition += mImageUrls.size();
        } else {
            realPosition = (position - 1) % mImages.size();
            if (realPosition < 0)
                realPosition += mImages.size();
        }

        return realPosition;
    }

    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset,
                                   int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            if (mIsImageUrl) {
                mCurrentPositon = position % (mImageUrls.size() + 2);
            } else {
                mCurrentPositon = position % (mImages.size() + 2);
            }
            switchToPoint(toRealPosition(mCurrentPositon));
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            int current = mViewPager.getCurrentItem();
            int lastReal = mViewPager.getAdapter().getCount() - 2;

            if (state == ViewPager.SCROLL_STATE_IDLE) {
                if (current == 0) {
                    mViewPager.setCurrentItem(lastReal, false);
                } else if (current == lastReal + 1) {
                    mViewPager.setCurrentItem(1, false);
                }
            } else if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                if (current == lastReal + 1) {
                    mViewPager.setCurrentItem(1, false);
                } else if (current == 0) {
                    mViewPager.setCurrentItem(lastReal, false);
                }
            }
        }
    };


    private class AdPageAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            //当只有一张图片时返回1
            if (mIsOneImg) {
                return 1;
            }
            //当为网络图片，返回网页图片长度
            if (mIsImageUrl)
                return mImageUrls.size() + 2;
            //当为本地图片，返回本地图片长度
            return mImages.size() + 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            ImageView imageView = new ImageView(getContext());
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(toRealPosition(position));
                    }
                }
            });
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            if (mIsImageUrl) {
                Glide.with(getContext()).load(mImageUrls.get(toRealPosition(position))).asBitmap().into(imageView);
            } else {
                imageView.setImageResource(mImages.get(toRealPosition(position)));
            }
            container.addView(imageView);

            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    /**
     * 添加指示点
     */
    private void addPoints() {
        //设置指示器容器是否可见
        if (mPointsIsVisible) {
            mPointContainerRl.setVisibility(View.VISIBLE);
        }

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LWC, LWC);
        lp.setMargins(10, 10, 10, 10);
        ImageView imageView;
        int length = mIsImageUrl ? mImageUrls.size() : mImages.size();
        for (int i = 0; i < length; i++) {
            imageView = new ImageView(getContext());
            imageView.setLayoutParams(lp);
            imageView.setImageResource(mPointDrawableResId);
            mPointRealContainerLl.addView(imageView);
        }
        switchToPoint(0);
    }

    /**
     * 切换指示器
     */
    private void switchToPoint(final int currentPoint) {
        for (int i = 0; i < mPointRealContainerLl.getChildCount(); i++) {
            mPointRealContainerLl.getChildAt(i).setEnabled(false);
        }
        mPointRealContainerLl.getChildAt(currentPoint).setEnabled(true);

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mAutoPlayAble && !mIsOneImg) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    stopAutoPlay();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_OUTSIDE:
                    startAutoPlay();
                    break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 开始播放
     */
    public void startAutoPlay() {
        if (mAutoPlayAble && !mIsAutoPlaying) {
            mIsAutoPlaying = true;
            mAutoPlayHandler.sendEmptyMessageDelayed(WAIT_AUTO_PLAY, mAutoPalyTime);
        }
    }

    /**
     * 停止播放
     */
    public void stopAutoPlay() {
        if (mAutoPlayAble && mIsAutoPlaying) {
            mIsAutoPlaying = false;
            mAutoPlayHandler.removeMessages(WAIT_AUTO_PLAY);
        }
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == View.VISIBLE) {
            if (mIsImageUrl && mImageUrls != null && mImageUrls.size() > 1) {
                startAutoPlay();
            } else if (!mIsImageUrl && mImages != null && mImages.size() > 1) {
                startAutoPlay();
            }
        } else {
            stopAutoPlay();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAutoPlayHandler.removeCallbacksAndMessages(null);
    }
}
