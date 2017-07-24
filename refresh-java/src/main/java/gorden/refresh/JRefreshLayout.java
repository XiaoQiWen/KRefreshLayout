package gorden.refresh;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.OverScroller;
import android.widget.ScrollView;

/**
 * Universal pull down the refresh frame
 * version 1.1
 */

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class JRefreshLayout extends ViewGroup {
    private static final String LOG_TAG = "LOG_JRefreshLayout";

    private static final int MAX_OFFSET = 30;//单次最大偏移量

    private JRefreshHeader mHeader;
    private View mHeaderView;
    private View mContentView;

    private OverScroller mScroller;
    private ValueAnimator mOffsetAnimator;
    private GestureDetectorCompat mGesture;

    private int mCurrentOffset;
    private int mLastFlingY;
    private float mInitialDownY;

    //状态参数↓
    private boolean mRefreshing = false;//是否处于刷新中
    private boolean mIsReset = true;//刷新完成后是否重置
    private boolean mIsBeingDragged = true;
    private boolean mIsFling = false;
    private boolean mGestureExecute = false;
    private boolean mNestedScrollExecute = false;
    private boolean mNestedScrollInProgress = false;

    //可配置参数,提供set方法
    private int defaultRefreshHeight;
    private int defaultMaxOffset;
    private long mDurationOffset = 200;
    private boolean mKeepHeaderWhenRefresh = true;
    private boolean mIsPinContent = false;
    private boolean mRefreshEnable = true;
    private int mTouchSlop;
    private int mFlingSlop = 1000;
    private int mHeaderOffset = 0;

    private JRefreshListener mRefreshListener;
    private JScrollListener mScrollListener;

    public JRefreshLayout(Context context) {
        this(context, null);
    }

    public JRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = new OverScroller(context);
        mGesture = new GestureDetectorCompat(context, new RefreshGestureListener());
        mGesture.setIsLongpressEnabled(false);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop() * 2;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.JRefreshLayout);
        mIsPinContent = a.getBoolean(R.styleable.JRefreshLayout_j_pin_content, false);
        mKeepHeaderWhenRefresh = a.getBoolean(R.styleable.JRefreshLayout_j_keep_header, true);
        mDurationOffset = a.getInt(R.styleable.JRefreshLayout_j_duration_offset, 200);
        mRefreshEnable = a.getBoolean(R.styleable.JRefreshLayout_j_refresh_enable, true);
        defaultRefreshHeight = a.getLayoutDimension(R.styleable.JRefreshLayout_j_def_refresh_height, Integer.MAX_VALUE);
        defaultMaxOffset = a.getLayoutDimension(R.styleable.JRefreshLayout_j_def_max_offset, defaultMaxOffset);
        a.recycle();
    }

    @Override
    protected void onFinishInflate() {
        int childCount = getChildCount();
        if (childCount > 2)
            throw new IllegalStateException("JRefreshLayout111 can only accommodate two elements");
        else if (childCount == 1) {
            mContentView = getChildAt(0);
        } else if (childCount == 2) {
            if (getChildAt(0) instanceof JRefreshHeader) {
                mHeader = (JRefreshHeader) getChildAt(0);
                mHeaderView = (View) mHeader;
            }
            mContentView = getChildAt(1);
        }

        if (mHeaderView != null)
            mHeaderView.bringToFront();
        super.onFinishInflate();
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @SuppressWarnings("WeakerAccess")
    public static class LayoutParams extends MarginLayoutParams {

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mHeaderView != null && !isInEditMode()) {
            LayoutParams lp = (LayoutParams) mHeaderView.getLayoutParams();
            int childLeft = getPaddingLeft() + lp.leftMargin;
            int childTop = getPaddingTop() + lp.topMargin - mHeaderView.getMeasuredHeight() + mCurrentOffset + mHeaderOffset;
            int childRight = childLeft + mHeaderView.getMeasuredWidth();
            int childBottom = childTop + mHeaderView.getMeasuredHeight();
            mHeaderView.layout(childLeft, childTop, childRight, childBottom);
        }

        if (mContentView != null) {
            LayoutParams lp = (LayoutParams) mContentView.getLayoutParams();
            int childLeft = getPaddingLeft() + lp.leftMargin;
            int childTop = getPaddingTop() + lp.topMargin + (mIsPinContent ? 0 : mCurrentOffset);
            int childRight = childLeft + mContentView.getMeasuredWidth();
            int childBottom = childTop + mContentView.getMeasuredHeight();
            mContentView.layout(childLeft, childTop, childRight, childBottom);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                cancelAnimator();
                mIsFling = false;
                mLastFlingY = 0;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                //如果NestedScroll未真正执行，则结束移动
                if (!mNestedScrollExecute && !mGestureExecute) {
                    finishSpinner();
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!isEnabled() || !mRefreshEnable)
            return false;

        if (mNestedScrollInProgress || canChildScrollUp())
            return false;

        if (mRefreshing && mIsPinContent && mKeepHeaderWhenRefresh)
            return false;

        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mIsBeingDragged = false;
                mInitialDownY = event.getY();
                mGesture.onTouchEvent(event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mIsBeingDragged && event.getY() - mInitialDownY > mTouchSlop) {
                    mIsBeingDragged = true;
                }
                if (mCurrentOffset > 0 && !mIsBeingDragged) {
                    mIsBeingDragged = true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                break;
        }
        return mIsBeingDragged;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {
        if ((android.os.Build.VERSION.SDK_INT < 21 && mContentView instanceof AbsListView)
                || (mContentView != null && !ViewCompat.isNestedScrollingEnabled(mContentView))) {

        } else {
            super.requestDisallowInterceptTouchEvent(b);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() || mNestedScrollExecute || canChildScrollUp())
            return false;
        mGesture.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (!mIsFling && mGestureExecute) {
                finishSpinner();
            }
            mGestureExecute = false;
        }
        return true;
    }

    private class RefreshGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            mGestureExecute = true;
            int maxOffset = mHeader == null ? defaultMaxOffset == -1 ? getHeight() : defaultMaxOffset : mHeader.maxOffsetHeight();
            if ((mCurrentOffset == 0 && distanceY > 0) || mCurrentOffset == maxOffset && distanceY < 0)
                return super.onScroll(e1, e2, distanceX, distanceY);

            int offset = -calculateOffset((int) distanceY);
            if (mCurrentOffset + offset > maxOffset) {
                offset = maxOffset - mCurrentOffset;
            } else if (mCurrentOffset + offset < 0) {
                offset = -mCurrentOffset;
            }
            moveView(offset);
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            mGestureExecute = true;
            int refreshHeight = mHeader == null ? defaultRefreshHeight : mHeader.refreshHeight();
            if (velocityY > 0 && (!mRefreshing || !mKeepHeaderWhenRefresh || mCurrentOffset >= refreshHeight)) {
                return super.onFling(e1, e2, velocityX, velocityY);
            }
            if (Math.abs(velocityY) > mFlingSlop) {
                mIsFling = true;
                mScroller.fling(0, 0, (int) velocityX, (int) -velocityY, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
                invalidate();
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    private boolean canChildScrollUp() {
        return mContentView != null && ViewCompat.canScrollVertically(mContentView, -1);
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return isEnabled() && mRefreshEnable && !(mRefreshing && mIsPinContent && mKeepHeaderWhenRefresh)
                && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        mNestedScrollExecute = false;
        mNestedScrollInProgress = true;
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        mNestedScrollExecute = true;
        if (mCurrentOffset > 0 && dy > 0) {
            int offset = dy > mCurrentOffset ? mCurrentOffset : dy;
            consumed[1] = dy > mCurrentOffset ? dy - mCurrentOffset : dy;
            moveView(-offset);
        }
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        int maxOffset = mHeader == null ? defaultMaxOffset == -1 ? getHeight() : defaultMaxOffset : mHeader.maxOffsetHeight();
        if (dyUnconsumed < 0 && !canChildScrollUp() && mCurrentOffset < maxOffset) {
            if (mCurrentOffset - dyUnconsumed > maxOffset) {
                dyUnconsumed = mCurrentOffset - maxOffset;
            }
            int offset = -calculateOffset(dyUnconsumed);
            moveView(offset);
        }
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        mNestedScrollExecute = true;
        //如果当前偏移量大于0，则交给KrefreshLayout处理Fling事件
        if (mCurrentOffset > 0) {
            int refreshHeight = mHeader == null ? defaultRefreshHeight : mHeader.refreshHeight();
            if (velocityY < 0 && (!mRefreshing || !mKeepHeaderWhenRefresh || mCurrentOffset >= refreshHeight)) {
                return true;
            }
            if (Math.abs(velocityY) > mFlingSlop) {
                mIsFling = true;
                mScroller.fling(0, 0, (int) velocityX, (int) velocityY, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
                invalidate();
            }
            return true;
        } else
            return false;
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        //如果向上滚动，且处于刷新过程中，监听Fling过程
        if (mRefreshing && velocityY < -mTouchSlop && mKeepHeaderWhenRefresh) {
            mIsFling = true;
            mScroller.fling(0, 0, (int) velocityX, (int) velocityY, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
            invalidate();
        }
        return true;
    }

    @Override
    public void onStopNestedScroll(View child) {
        if (!mIsFling && mNestedScrollExecute) {
            finishSpinner();
        }
        mNestedScrollExecute = false;
        mNestedScrollInProgress = false;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset() && mIsFling) {
            //本次Fling移动距离(<0向下滚动、>0向上滚动)
            int offset = mLastFlingY - mScroller.getCurrY();
            int refreshHeight = mHeader == null ? defaultRefreshHeight : mHeader.refreshHeight();
            int maxOffset = mHeader == null ? defaultMaxOffset == -1 ? getHeight() : defaultMaxOffset : mHeader.maxOffsetHeight();
            int mFlingMaxHeight = offset > 0 ? refreshHeight : maxOffset;
            //记录上次Fling的Y值
            mLastFlingY = mScroller.getCurrY();

            if (mCurrentOffset > 0 || (offset > 0 && !canChildScrollUp())) {
                offset = mCurrentOffset + offset > mFlingMaxHeight ? mFlingMaxHeight - mCurrentOffset : mCurrentOffset + offset < 0 ? -mCurrentOffset : offset;
                moveView(offset);
                if (mCurrentOffset >= mFlingMaxHeight) {
                    mScroller.forceFinished(true);
                }
            } else if (offset < 0) {
                if (mContentView instanceof RecyclerView) {
                    ((RecyclerView) mContentView).fling(0, (int) mScroller.getCurrVelocity());
                } else if (mContentView instanceof NestedScrollView) {
                    ((NestedScrollView) mContentView).fling((int) mScroller.getCurrVelocity());
                } else if (mContentView instanceof ScrollView) {
                    ((ScrollView) mContentView).fling((int) mScroller.getCurrVelocity());
                }
                mScroller.forceFinished(true);
            }
            invalidate();
        } else if (mIsFling) {
            Log.d(LOG_TAG, "mScroll fling complete mCurrentOffset is " + mCurrentOffset);
            mIsFling = false;
            finishSpinner();
        }
    }

    /**
     * 计算实际偏移量
     */
    private int calculateOffset(int offset) {
        //下拉阻力(0f-1f) 越小阻力越大，当前计算公式:1-mDistanceY/maxheight
        int maxOffset = mHeader == null ? defaultMaxOffset == -1 ? getHeight() : defaultMaxOffset : mHeader.maxOffsetHeight();
        float downResistance = offset > 0 ? 0.8f : 1f - (float) mCurrentOffset / maxOffset;
        if (offset > 0) {
            offset = Math.min(MAX_OFFSET, (int) Math.ceil(downResistance * offset));
        } else {
            offset = Math.max(-MAX_OFFSET, (int) Math.floor(downResistance * offset));
        }
        return offset;
    }

    /**
     * 移动视图
     *
     * @param offset 偏移量
     *               //     * @param requiresUpdate 是否需要更新
     */
    private void moveView(int offset) {
        boolean invalidate = false;
        int refreshHeight = mHeader == null ? defaultRefreshHeight : mHeader.refreshHeight();
        if (!mRefreshing && mCurrentOffset == 0 && offset > 0) {
            if (mHeader != null) mHeader.onPrepare(this);
        }

        if (mCurrentOffset > getHeight() || mCurrentOffset == 0) {
            invalidate = true;
        }

        mCurrentOffset += offset;
        if (mHeaderView != null) mHeaderView.offsetTopAndBottom(offset);
        if (!mIsPinContent)
            mContentView.offsetTopAndBottom(offset);
        if (invalidate) invalidate();
        if (mHeader != null)
            mHeader.onScroll(this, mCurrentOffset, (float) mCurrentOffset / refreshHeight, mRefreshing);
        if (mScrollListener != null)
            mScrollListener.onScroll(offset, mCurrentOffset, (float) mCurrentOffset / refreshHeight, mRefreshing);
        if (!mRefreshing && offset < 0 && mCurrentOffset == 0) {
            if (mHeader != null) mHeader.onReset(this);
            mIsReset = true;
        }
    }

    /**
     * 取消offset动画
     */
    private void cancelAnimator() {
        if (mOffsetAnimator != null && mOffsetAnimator.isRunning()) {
            mOffsetAnimator.cancel();
        }
    }

    /**
     * offset动画更新监听
     */
    private ValueAnimator.AnimatorUpdateListener mAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            int value = (int) animation.getAnimatedValue();
            moveView(value - mCurrentOffset);
        }
    };

    /**
     * 结束下拉
     */
    private void finishSpinner() {
        if (mCurrentOffset <= 0) return;
        Log.d(LOG_TAG, "finishSpinner mCurrentOffset is " + mCurrentOffset + " , mRefreshing is " + mRefreshing);
        final int target;
        int refreshHeight = mHeader == null ? defaultRefreshHeight : mHeader.refreshHeight();
        if (mRefreshing) {
            target = mCurrentOffset >= refreshHeight / 2 ? refreshHeight : 0;
        } else {
            target = mCurrentOffset >= refreshHeight && mIsReset ? refreshHeight : 0;
            if (mCurrentOffset >= refreshHeight && mIsReset) {
                mRefreshing = true;//开始刷新
                mIsReset = false;
                if (mHeader != null) mHeader.onRefresh(this);
                if (mRefreshListener != null)
                    mRefreshListener.onRefresh(this);
            }
        }
        animTo(target);
    }

    /**
     * 动画方式移动
     *
     * @param target 目标位置
     */
    private void animTo(int target) {
        if (mOffsetAnimator == null) {
            mOffsetAnimator = new ValueAnimator();
            mOffsetAnimator.addUpdateListener(mAnimatorUpdateListener);
        }

        cancelAnimator();

        if (!mKeepHeaderWhenRefresh) target = 0;

        if (mCurrentOffset == target) {
            return;
        }
        Log.d(LOG_TAG, "animTo " + mCurrentOffset + " to " + target);
        mOffsetAnimator.setDuration(mDurationOffset);
        mOffsetAnimator.setIntValues(mCurrentOffset, target);
        mOffsetAnimator.start();
    }


    @SuppressWarnings("WeakerAccess")
    public interface JRefreshListener {
        void onRefresh(JRefreshLayout refreshLayout);
    }

    @SuppressWarnings("WeakerAccess")
    public interface JScrollListener {
        /**
         * @param offset     本次的偏移量
         * @param distance   总的偏移量
         * @param percent    偏移比率
         * @param refreshing 是否在刷新
         */
        void onScroll(int offset, int distance, float percent, boolean refreshing);
    }


    //开放Api

    public void setJRefreshListener(JRefreshListener refreshListener) {
        mRefreshListener = refreshListener;
    }

    public void setJScrollListener(JScrollListener scrollListener) {
        mScrollListener = scrollListener;
    }

    public void setPinContent(boolean pinContent) {
        mIsPinContent = pinContent;
    }

    public void setKeepHeaderWhenRefresh(boolean keep) {
        mKeepHeaderWhenRefresh = keep;
    }

    public void setRefreshEnable(boolean enable) {
        mRefreshEnable = enable;
    }

    public void setDurationOffset(long duration) {
        mDurationOffset = duration;
    }

    public void setHeaderView(JRefreshHeader headerView) {
        setHeaderView(headerView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    public void setHeaderView(JRefreshHeader headerView, int width, int height) {
        LayoutParams params = generateDefaultLayoutParams();
        params.width = width;
        params.height = height;
        setHeaderView(headerView, params);
    }

    public void setHeaderView(JRefreshHeader headerView, LayoutParams params) {
        removeHeader();
        mHeader = headerView;
        mHeaderView = (View) mHeader;
        addView(mHeaderView, 0, params);
        mHeaderView.bringToFront();
    }

    public JRefreshHeader getHeader() {
        return mHeader;
    }

    public void removeHeader() {
        if (mHeaderView != null) {
            removeView(mHeaderView);
        }
    }

    public void setTouchSlop(int mTouchSlop) {
        this.mTouchSlop = mTouchSlop;
    }

    public void setFlingSlop(int mFlingSlop) {
        this.mFlingSlop = mFlingSlop;
    }

    public void setHeaderOffset(int mHeaderOffset) {
        this.mHeaderOffset = mHeaderOffset;
    }

    public void setDefaultRefreshHeight(int defaultRefreshHeight) {
        this.defaultRefreshHeight = defaultRefreshHeight;
    }

    public void setDefaultMaxOffset(int defaultMaxOffset) {
        this.defaultMaxOffset = defaultMaxOffset;
    }

    /**
     * 自动刷新
     */
    public void startRefresh() {
        if (!mRefreshing && mRefreshEnable) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    mRefreshing = true;
                    mIsReset = false;
                    if (mHeader != null) mHeader.onRefresh(JRefreshLayout.this);
                    if (mRefreshListener != null) mRefreshListener.onRefresh(JRefreshLayout.this);
                    mContentView.scrollTo(0, 0);
                    animTo(mHeader == null ? defaultRefreshHeight : mHeader.refreshHeight());
                }
            }, 100);
        }
    }

    /**
     * 刷新完成
     */
    public void refreshComplete(boolean isSuccess) {
        if (mRefreshing) {
            if (mHeader != null) mHeader.onComplete(this, isSuccess);
            mRefreshing = false;
            if (mCurrentOffset == 0) {
                mIsReset = true;
                cancelAnimator();
                if (mHeader != null) mHeader.onReset(this);
            } else {
                //刷新完成停滞时间
                long retention = mHeader == null ? 0 : isSuccess ? mHeader.succeedRetention() : mHeader.failingRetention();
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        animTo(0);
                    }
                }, retention);
            }
        }
    }
}
