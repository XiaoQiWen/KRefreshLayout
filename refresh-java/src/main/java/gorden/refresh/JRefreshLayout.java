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

/**
 * Universal pull down the refresh frame
 * version 1.1
 */

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class JRefreshLayout extends ViewGroup {
    private static final String LOG_TAG = "LOG_JRefreshLayout";

    private JRefreshHeader mHeader;
    private View mContentView;

    private int mCurrentOffset;
    private int mLastFlingY;

    private boolean mRefreshing = false;//是否处于刷新中
    private boolean mIsReset = true;//刷新完成后是否重置
    private boolean mIsBeingDragged = true;
    private boolean mIsFling = false;

    /**
     * nestedScroll 是否执行
     */
    private boolean nestedScrollExecute = false;

    private static final int MAX_OFFSET = 30;//单次最大偏移量
    private long mDurationOffset = 200;
    private float mInitialDownY;
    private int mTouchSlop;
    private final int mFlingSlop = 2000;

    //下拉刷新过程是否钉住contentView
    private boolean mIsPinContent = false;
    //刷新时保持头部
    private boolean mKeepHeaderWhenRefresh = true;
    private boolean mRefreshEnable = true;

    private OverScroller mScroller;
    private ValueAnimator mOffsetAnimator;
    private GestureDetectorCompat mGesture;

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
        mIsPinContent = a.getBoolean(R.styleable.JRefreshLayout_j_pincontent, false);
        mKeepHeaderWhenRefresh = a.getBoolean(R.styleable.JRefreshLayout_j_keepheader, true);
        mDurationOffset = a.getInt(R.styleable.JRefreshLayout_j_durationoffset, 200);
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
            View a = getChildAt(0);
            if (a instanceof JRefreshHeader) {
                mHeader = (JRefreshHeader) a;
            }
            mContentView = getChildAt(1);
        }

        if (mHeader != null)
            mHeader.getView().bringToFront();
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
        if (mHeader != null && !isInEditMode()) {
            LayoutParams lp = (LayoutParams) mHeader.getView().getLayoutParams();
            int childLeft = getPaddingLeft() + lp.leftMargin;
            int childTop = getPaddingTop() + lp.topMargin - mHeader.getView().getMeasuredHeight() + mCurrentOffset;
            int childRight = childLeft + mHeader.getView().getMeasuredWidth();
            int childBottom = childTop + mHeader.getView().getMeasuredHeight();
            mHeader.getView().layout(childLeft, childTop, childRight, childBottom);
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
                if (!nestedScrollExecute && mCurrentOffset > 0) {
                    finishSpinner();
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!isEnabled() || nestedScrollExecute || mContentView instanceof NestedScrollingChild || canChildScrollUp() || mHeader == null)
            return false;

        if (!mRefreshEnable) return false;

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
        if (!isEnabled() || mHeader == null || nestedScrollExecute || canChildScrollUp())
            return false;
        mGesture.onTouchEvent(event);
        return true;
    }

    private class RefreshGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if ((mCurrentOffset == 0 && distanceY > 0) || mCurrentOffset == mHeader.maxOffsetHeight() && distanceY < 0)
                return super.onScroll(e1, e2, distanceX, distanceY);
            int offset = -calculateOffset((int) distanceY);

            if (mCurrentOffset + offset > mHeader.maxOffsetHeight()) {
                offset = mHeader.maxOffsetHeight() - mCurrentOffset;
            } else if (mCurrentOffset + offset < 0) {
                offset = -mCurrentOffset;
            }
            moveView(offset);
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

    private boolean canChildScrollUp() {
        return mContentView != null && ViewCompat.canScrollVertically(mContentView, -1);
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return isEnabled() && mRefreshEnable && mHeader != null && !(mRefreshing && mIsPinContent && mKeepHeaderWhenRefresh)
                && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        nestedScrollExecute = false;
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        nestedScrollExecute = true;
        if (mCurrentOffset > 0 && dy > 0) {
            int offset = dy > mCurrentOffset ? mCurrentOffset : dy;
            consumed[1] = dy > mCurrentOffset ? dy - mCurrentOffset : dy;
            moveView(-offset);
        }
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        if (dyUnconsumed < 0 && !canChildScrollUp() && mCurrentOffset < mHeader.maxOffsetHeight()) {
            if (mCurrentOffset - dyUnconsumed > mHeader.maxOffsetHeight()) {
                dyUnconsumed = mCurrentOffset - mHeader.maxOffsetHeight();
            }
            int offset = -calculateOffset(dyUnconsumed);
            moveView(offset);
        }
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        nestedScrollExecute = true;
        //如果当前偏移量大于0，则交给KrefreshLayout处理Fling事件
        if (mCurrentOffset > 0) {
            if (velocityY<0&&(!mRefreshing||!mKeepHeaderWhenRefresh||mCurrentOffset>=mHeader.refreshHeight())){
                return true;
            }
            if (Math.abs(velocityY)>mFlingSlop){
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
        if (!mIsFling && mCurrentOffset > 0 && nestedScrollExecute) {
            finishSpinner();
        }
        nestedScrollExecute = false;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset() && mIsFling) {
            //本次Fling移动距离(<0向下滚动、>0向上滚动)
            int offset = mLastFlingY - mScroller.getCurrY();
            int mFlingMaxHeight = offset>0?mHeader.refreshHeight():mHeader.maxOffsetHeight();
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
                }
                mScroller.forceFinished(true);
            }
            invalidate();
        } else if (mIsFling) {
            mIsFling = false;
            Log.d(LOG_TAG, "mScroll fling complete mCurrentOffset is " + mCurrentOffset);
            if (mCurrentOffset > 0)
                finishSpinner();
        }
    }

    /**
     * 计算实际偏移量
     */
    private int calculateOffset(int offset) {
        //下拉阻力(0f-1f) 越小阻力越大，当前计算公式:1-mDistanceY/maxheight
        float downResistance = offset > 0 ? 0.8f : 1f - (float) mCurrentOffset / mHeader.maxOffsetHeight();
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
        if (!mRefreshing && mCurrentOffset == 0 && offset > 0) {
            mHeader.onPrepare(this);
            invalidate = true;
        }

        if (mCurrentOffset > getHeight()) {
            invalidate = true;
        }

        mCurrentOffset += offset;
        mHeader.getView().offsetTopAndBottom(offset);
        if (!mIsPinContent)
            mContentView.offsetTopAndBottom(offset);
        if (invalidate) invalidate();
        mHeader.onScroll(this, mCurrentOffset, (float) mCurrentOffset / mHeader.refreshHeight(), mRefreshing);

        if (!mRefreshing && offset < 0 && mCurrentOffset == 0) {
            mHeader.onReset(this);
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
        Log.d(LOG_TAG, "finishSpinner mCurrentOffset is " + mCurrentOffset + " , mRefreshing is " + mRefreshing);
        final int target;
        if (mRefreshing) {
            target = mCurrentOffset >= mHeader.refreshHeight() / 2 ? mHeader.refreshHeight() : 0;
        } else {
            target = mCurrentOffset >= mHeader.refreshHeight() && mIsReset ? mHeader.refreshHeight() : 0;
            if (mCurrentOffset >= mHeader.refreshHeight() && mIsReset) {
                mRefreshing = true;//开始刷新
                mIsReset = false;
                mHeader.onRefresh(this);
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

        if (mOffsetAnimator.isRunning()) {
            mOffsetAnimator.cancel();
        }
        if (!mKeepHeaderWhenRefresh) target = 0;

        if (mCurrentOffset == target) {
            return;
        }
        Log.d(LOG_TAG, "animTo " + mCurrentOffset + " to " + target);
        mOffsetAnimator.setDuration(mDurationOffset);
        mOffsetAnimator.setIntValues(mCurrentOffset, target);
        mOffsetAnimator.start();
    }

    private JRefreshListener mRefreshListener;

    @SuppressWarnings("WeakerAccess")
    public interface JRefreshListener {
        void onRefresh(JRefreshLayout refreshLayout);
    }

    public void setJRefreshListener(JRefreshListener refreshListener) {
        mRefreshListener = refreshListener;
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
        if (mHeader != null) {
            removeView(mHeader.getView());
        }
        mHeader = headerView;
        addView(mHeader.getView(), 0, params);
        mHeader.getView().bringToFront();
    }

    /**
     * 自动刷新
     */
    public void startRefresh() {
        if (!mRefreshing && mHeader != null) {
            mRefreshing = true;
            mIsReset = false;
            mHeader.onRefresh(this);
            if (mRefreshListener != null) mRefreshListener.onRefresh(this);
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    mContentView.scrollTo(0, 0);
                    animTo(mHeader.refreshHeight());
                }
            }, 100);
        }
    }

    /**
     * 刷新完成
     */
    public void refreshComplete(boolean isSuccess) {
        if (mRefreshing && mHeader != null) {
            mHeader.onComplete(this, isSuccess);
            if (mCurrentOffset == 0) {
                mRefreshing = false;
                mIsReset = true;
                mHeader.onReset(this);
            } else {
                mRefreshing = false;
                //刷新完成停滞时间
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        animTo(0);
                    }
                }, isSuccess ? mHeader.succeedRetention() : mHeader.failingRetention());
            }
        }
    }
}
