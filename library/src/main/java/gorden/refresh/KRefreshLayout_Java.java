//package gorden.refresh;
//
//import android.animation.ValueAnimator;
//import android.content.Context;
//import android.content.res.TypedArray;
//import android.support.v4.view.GestureDetectorCompat;
//import android.support.v4.view.MotionEventCompat;
//import android.support.v4.view.NestedScrollingChild;
//import android.support.v4.view.NestedScrollingParent;
//import android.support.v4.view.ViewCompat;
//import android.support.v4.widget.NestedScrollView;
//import android.support.v7.widget.RecyclerView;
//import android.util.AttributeSet;
//import android.util.Log;
//import android.view.GestureDetector;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.ViewConfiguration;
//import android.view.ViewGroup;
//import android.widget.AbsListView;
//import android.widget.OverScroller;
//
//import static android.support.v4.widget.ViewDragHelper.INVALID_POINTER;
//
///**
// * Universal pull down the refresh frame
// */
//@SuppressWarnings("unused")
//public class KRefreshLayout_Java extends ViewGroup implements NestedScrollingParent {
//    private static final String LOG_TAG = "LOG_KRefreshLayout";
//    private KRefreshHeader mHeaderView;
//    private View mContentView;
//
//    private int mCurrentOffset;
//    private int mLastFlingY;
//    private boolean mRefreshing = false;//是否处于刷新中
//    private boolean mIsReset = true;//刷新完成后是否重置
//    private boolean mIsFling = false;
//    private int mFlingMaxHeight = 0;
//
//    /**
//     * nestedScroll 是否执行
//     */
//    private boolean nestedScrollExecute = false;
//
//    private static final int MAX_OFFSET = 30;//单次最大偏移量
//    private long mDurationOffset = 200;
//
//    private boolean mIsBeingDragged = true;
//    private int mActivePointerId = INVALID_POINTER;
//    private float mInitialDownY;
//    private int mTouchSlop;
//
//    //下拉刷新过程是否钉住contentView
//    private boolean mIsPinContent = false;
//    //刷新时保持头部
//    private boolean mKeepHeaderWhenRefresh = true;
//
//    private OverScroller mScroller;
//    private ValueAnimator mOffsetAnimator;
//    private GestureDetectorCompat mGesture;
//
//    public KRefreshLayout_Java(Context context) {
//        this(context, null);
//    }
//
//    public KRefreshLayout_Java(Context context, AttributeSet attrs) {
//        this(context, attrs, 0);
//    }
//
//    public KRefreshLayout_Java(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        mScroller = new OverScroller(context);
//        mGesture = new GestureDetectorCompat(context, new RefreshGestureListener());
//        mGesture.setIsLongpressEnabled(false);
//        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop() * 2;
//
//        TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.KRefreshLayout);
//        mIsPinContent = a.getBoolean(R.styleable.KRefreshLayout_k_pincontent,false);
//        mKeepHeaderWhenRefresh = a.getBoolean(R.styleable.KRefreshLayout_k_keepheader,true);
//        mDurationOffset = a.getInt(R.styleable.KRefreshLayout_k_durationoffset,200);
//        a.recycle();
//
//    }
//
//    @Override
//    protected void onFinishInflate() {
//        int childCount = getChildCount();
//
//        if (childCount > 2)
//            throw new IllegalStateException("KRefreshLayout111 can only accommodate two elements");
//        else if (childCount == 1) {
//            mContentView = getChildAt(0);
//        } else if (childCount == 2) {
//            View a = getChildAt(0);
//            if (a instanceof KRefreshLayout){
//                mHeaderView = (KRefreshHeader) a;
//            }
//            mHeaderView = (KRefreshHeader) getChildAt(0);
//            mContentView = getChildAt(1);
//        }
//
//        if (mIsPinContent && mHeaderView != null)
//            mHeaderView.getView().bringToFront();
//        super.onFinishInflate();
//    }
//
//    @Override
//    protected LayoutParams generateDefaultLayoutParams() {
//        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//    }
//
//    @Override
//    public LayoutParams generateLayoutParams(AttributeSet attrs) {
//        return new LayoutParams(getContext(), attrs);
//    }
//
//    @Override
//    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
//        return new LayoutParams(p);
//    }
//
//    @SuppressWarnings("WeakerAccess")
//    public static class LayoutParams extends MarginLayoutParams {
//
//        public LayoutParams(Context c, AttributeSet attrs) {
//            super(c, attrs);
//        }
//
//        public LayoutParams(int width, int height) {
//            super(width, height);
//        }
//
//        public LayoutParams(MarginLayoutParams source) {
//            super(source);
//        }
//
//        public LayoutParams(ViewGroup.LayoutParams source) {
//            super(source);
//        }
//    }
//
//    @Override
//    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        if (mHeaderView != null) {
//            LayoutParams lp = (LayoutParams) mHeaderView.getView().getLayoutParams();
//            int childLeft = getPaddingLeft() + lp.leftMargin;
//            int childTop = getPaddingTop() + lp.topMargin - mHeaderView.getView().getMeasuredHeight() + mCurrentOffset;
//            int childRight = childLeft + mHeaderView.getView().getMeasuredWidth();
//            int childBottom = childTop + mHeaderView.getView().getMeasuredHeight();
//            mHeaderView.getView().layout(childLeft, childTop, childRight, childBottom);
//        }
//
//        if (mContentView != null) {
//            LayoutParams lp = (LayoutParams) mContentView.getLayoutParams();
//            int childLeft = getPaddingLeft() + lp.leftMargin;
//            int childTop = getPaddingTop() + lp.topMargin + (mIsPinContent ? 0 : mCurrentOffset);
//            int childRight = childLeft + mContentView.getMeasuredWidth();
//            int childBottom = childTop + mContentView.getMeasuredHeight();
//            mContentView.layout(childLeft, childTop, childRight, childBottom);
//        }
//    }
//
//
//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//
//        for (int i = 0; i < getChildCount(); i++) {
//            View child = getChildAt(i);
//            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
//        }
//
//    }
//
//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        switch (ev.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                cancelAnimator();
//                mIsFling = false;
//                break;
//            case MotionEvent.ACTION_UP:
//            case MotionEvent.ACTION_CANCEL:
//                //如果NestedScroll未真正执行，则结束移动
//                if (!nestedScrollExecute && mCurrentOffset > 0) {
//                    finishSpinner();
//                }
//                break;
//        }
//        return super.dispatchTouchEvent(ev);
//    }
//
//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent event) {
//        if (mContentView != null && mContentView instanceof NestedScrollingChild) {
//            return false;
//        }
//        if (!isEnabled() || nestedScrollExecute || canChildScrollUp() || mHeaderView == null)
//            return false;
//
//        if (mRefreshing && mIsPinContent && mKeepHeaderWhenRefresh)
//            return false;
//
//        int action = event.getAction();
//        int pointerIndex;
//        switch (action) {
//            case MotionEvent.ACTION_DOWN:
//                mActivePointerId = event.getPointerId(0);
//                mIsBeingDragged = mCurrentOffset != 0;
//                cancelAnimator();
//                if (mIsBeingDragged) {
//                    cancelAnimator();
//                }
//
//                pointerIndex = event.findPointerIndex(mActivePointerId);
//                if (pointerIndex < 0) {
//                    return false;
//                }
//                mInitialDownY = event.getY(pointerIndex);
//                break;
//            case MotionEvent.ACTION_MOVE:
//                if (mActivePointerId == INVALID_POINTER) {
//                    return false;
//                }
//                pointerIndex = event.findPointerIndex(mActivePointerId);
//                if (pointerIndex < 0) {
//                    return false;
//                }
//                final float y = event.getY(pointerIndex);
//                startDragging(y);
//                break;
//            case MotionEventCompat.ACTION_POINTER_UP:
//                onSecondaryPointerUp(event);
//                break;
//
//            case MotionEvent.ACTION_UP:
//            case MotionEvent.ACTION_CANCEL:
//                mIsBeingDragged = mCurrentOffset != 0;
//                mActivePointerId = INVALID_POINTER;
//                break;
//        }
//        return mIsBeingDragged;
//    }
//
//    @SuppressWarnings("StatementWithEmptyBody")
//    @Override
//    public void requestDisallowInterceptTouchEvent(boolean b) {
//        if ((android.os.Build.VERSION.SDK_INT < 21 && mContentView instanceof AbsListView)
//                || (mContentView != null && !ViewCompat.isNestedScrollingEnabled(mContentView))) {
//
//        } else {
//            super.requestDisallowInterceptTouchEvent(b);
//        }
//    }
//
//    private void startDragging(float y) {
//        final float yDiff = y - mInitialDownY;
//        if (yDiff > mTouchSlop && !mIsBeingDragged) {
//            cancelAnimator();
//            mIsBeingDragged = true;
//        }
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        if (!isEnabled() || mHeaderView == null || nestedScrollExecute || canChildScrollUp())
//            return false;
//        mGesture.onTouchEvent(event);
//        return true;
//    }
//
//    private void onSecondaryPointerUp(MotionEvent ev) {
//        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
//        final int pointerId = ev.getPointerId(pointerIndex);
//        if (pointerId == mActivePointerId) {
//            // This was our active pointer going up. Choose a new
//            // active pointer and adjust accordingly.
//            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
//            mActivePointerId = ev.getPointerId(newPointerIndex);
//        }
//    }
//
//    private class RefreshGestureListener extends GestureDetector.SimpleOnGestureListener {
//        @Override
//        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//            if ((mCurrentOffset == 0 && distanceY > 0) || mCurrentOffset == mHeaderView.maxOffsetHeight() && distanceY < 0)
//                return super.onScroll(e1, e2, distanceX, distanceY);
//            int offset = -calculateOffset((int) distanceY);
//
//            if (mCurrentOffset + offset > mHeaderView.maxOffsetHeight()) {
//                offset = mHeaderView.maxOffsetHeight() - mCurrentOffset;
//            } else if (mCurrentOffset + offset < 0) {
//                offset = -mCurrentOffset;
//            }
//            moveView(offset);
//            return super.onScroll(e1, e2, distanceX, distanceY);
//        }
//    }
//
//
//    // NestedScrollingParent
//
//    @Override
//    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
//        return isEnabled() && mHeaderView != null && !(mRefreshing && mIsPinContent && mKeepHeaderWhenRefresh)
//                && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
//    }
//
//    @Override
//    public void onNestedScrollAccepted(View child, View target, int axes) {
//        nestedScrollExecute = false;
//    }
//
//    @Override
//    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
//        nestedScrollExecute = true;
//        nestedPreScrollY = dy;
//        if (mCurrentOffset > 0 && dy > 0) {
//            int offset = dy > mCurrentOffset ? mCurrentOffset : dy;
//            consumed[1] = dy > mCurrentOffset ? dy - mCurrentOffset : dy;
//            moveView(-offset);
//        }
//    }
//
//    boolean temp = false;
//
//    int nestedPreScrollY = 0;
//
//    @Override
//    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
//        if (dyUnconsumed < 0 && !canChildScrollUp() && mCurrentOffset < mHeaderView.maxOffsetHeight()) {
//            if (mCurrentOffset - dyUnconsumed > mHeaderView.maxOffsetHeight()) {
//                dyUnconsumed = mCurrentOffset - mHeaderView.maxOffsetHeight();
//            }
//            int offset = -calculateOffset(dyUnconsumed);
//            moveView(offset);
//        }
//    }
//
//    @Override
//    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
//        nestedScrollExecute = true;
//
//        if (nestedPreScrollY*velocityY<0){
//            //莫名其妙的Bug,估计是Recyclerview存在的Bug
//            Log.e(LOG_TAG, "onNestedPreFling velocity orientation  is error  "+velocityY);
//            return true;
//        }
//        //如果当前偏移量大于0，则交给KrefreshLayout处理Fling事件
//        if (mCurrentOffset > 0) {
//            mLastFlingY = 0;
//            mIsFling = true;
//            mFlingMaxHeight = mHeaderView.maxOffsetHeight();
//            mScroller.fling(0, 0, (int) velocityX, (int) velocityY, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
//            invalidate();
//            return true;
//        } else
//            return false;
//    }
//
//    @Override
//    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
//        //如果向上滚动，且处于刷新过程中，监听Fling过程
//        if (mRefreshing && velocityY < 0 && mKeepHeaderWhenRefresh) {
//            mLastFlingY = 0;
//            mIsFling = true;
//            mFlingMaxHeight = mHeaderView.refreshHeight();
//            mScroller.fling(0, 0, (int) velocityX, (int) velocityY, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
//            invalidate();
//        }
//        return true;
//    }
//
//    @Override
//    public void onStopNestedScroll(View child) {
//        if (!mIsFling && mCurrentOffset > 0 && nestedScrollExecute) {
//            finishSpinner();
//        }
//        nestedScrollExecute = false;
//    }
//
//    /**
//     * 结束下拉
//     */
//    private void finishSpinner() {
//        Log.d(LOG_TAG, "finishSpinner mCurrentOffset is " + mCurrentOffset + " , mRefreshing is " + mRefreshing);
//        final int target;
//        if (mRefreshing) {
//            target = mCurrentOffset >= mHeaderView.refreshHeight() / 2 ? mHeaderView.refreshHeight() : 0;
//        } else {
//            target = mCurrentOffset >= mHeaderView.refreshHeight() && mIsReset ? mHeaderView.refreshHeight() : 0;
//            if (mCurrentOffset >= mHeaderView.refreshHeight() && mIsReset) {
//                mRefreshing = true;//开始刷新
//                mIsReset = false;
//                mHeaderView.onRefresh(this);
//                if (mRefreshListener != null)
//                    mRefreshListener.onRefresh(this);
//            }
//        }
//        animTo(target);
//    }
//
//
//    /**
//     * 动画方式移动
//     *
//     * @param target 目标位置
//     */
//    private void animTo(int target) {
//        if (mOffsetAnimator == null) {
//            mOffsetAnimator = new ValueAnimator();
//            mOffsetAnimator.addUpdateListener(mAnimatorUpdateListener);
//        }
//
//        if (mOffsetAnimator.isRunning()) {
//            mOffsetAnimator.cancel();
//        }
//        if (!mKeepHeaderWhenRefresh) target = 0;
//
//        if (mCurrentOffset == target) {
//            return;
//        }
//        Log.d(LOG_TAG, "animTo " + mCurrentOffset + " to " + target);
//        mOffsetAnimator.setDuration(mDurationOffset);
//        mOffsetAnimator.setIntValues(mCurrentOffset, target);
//        mOffsetAnimator.start();
//    }
//
//    /**
//     * 取消offset动画
//     */
//    private void cancelAnimator() {
//        if (mOffsetAnimator != null && mOffsetAnimator.isRunning()) {
//            mOffsetAnimator.cancel();
//        }
//    }
//
//    /**
//     * offset动画更新监听
//     */
//    private ValueAnimator.AnimatorUpdateListener mAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
//        @Override
//        public void onAnimationUpdate(ValueAnimator animation) {
//            int value = (int) animation.getAnimatedValue();
//            moveView(value - mCurrentOffset);
//        }
//    };
//
//    @Override
//    public void computeScroll() {
//        if (mScroller.computeScrollOffset() && mIsFling) {
//            //本次Fling移动距离(<0向下滚动、>0向上滚动)
//            int offset = mLastFlingY - mScroller.getCurrY();
//            //记录上次Fling的Y值
//            mLastFlingY = mScroller.getCurrY();
//
//            if (mCurrentOffset > 0 || (offset > 0 && !canChildScrollUp())) {
//                offset = mCurrentOffset + offset > mFlingMaxHeight ? mFlingMaxHeight - mCurrentOffset : mCurrentOffset + offset < 0 ? -mCurrentOffset : offset;
//                moveView(offset);
//                if (mCurrentOffset >= mFlingMaxHeight) {
//                    mScroller.forceFinished(true);
//                }
//            } else if (offset < 0) {
//                if (mContentView instanceof RecyclerView) {
//                    ((RecyclerView) mContentView).fling(0, (int) mScroller.getCurrVelocity());
//                } else if (mContentView instanceof NestedScrollView) {
//                    ((NestedScrollView) mContentView).fling((int) mScroller.getCurrVelocity());
//                }
//                mScroller.forceFinished(true);
//            }
//            invalidate();
//        } else if (mIsFling) {
//            mIsFling = false;
//            Log.d(LOG_TAG, "mScroll fling complete mCurrentOffset is " + mCurrentOffset);
//            if (mCurrentOffset > 0)
//                finishSpinner();
//        }
//    }
//
//    /**
//     * 移动视图
//     *
//     * @param offset 偏移量
//     *               //     * @param requiresUpdate 是否需要更新
//     */
//    private void moveView(int offset) {
//        if (!mRefreshing && mCurrentOffset == 0 && offset > 0) {
//            mHeaderView.onPrepare(this);
//        }
//
//        mCurrentOffset += offset;
//        mHeaderView.getView().offsetTopAndBottom(offset);
//        if (!mIsPinContent)
//            mContentView.offsetTopAndBottom(offset);
//
//        mHeaderView.onScroll(this, mCurrentOffset, (float) mCurrentOffset / mHeaderView.refreshHeight(), mRefreshing);
//
//        if (!mRefreshing && offset < 0 && mCurrentOffset == 0) {
//            mHeaderView.onReset(this);
//            mIsReset = true;
//        }
//    }
//
//    /**
//     * 计算实际偏移量
//     */
//    private int calculateOffset(int offset) {
//        //下拉阻力(0f-1f) 越小阻力越大，当前计算公式:1-mDistanceY/maxheight
//        float downResistance = offset > 0 ? 0.8f : 1f - (float) mCurrentOffset / mHeaderView.maxOffsetHeight();
//        if (offset > 0) {
//            offset = Math.min(MAX_OFFSET, (int) Math.ceil(downResistance * offset));
//        } else {
//            offset = Math.max(-MAX_OFFSET, (int) Math.floor(downResistance * offset));
//        }
//        return offset;
//    }
//
//    private boolean canChildScrollUp() {
//        return mContentView != null && ViewCompat.canScrollVertically(mContentView, -1);
//    }
//
//
//    @SuppressWarnings("WeakerAccess")
//    public interface KRefreshListener {
//        void onRefresh(KRefreshLayout refreshLayout);
//    }
//
//
//    private KRefreshListener mRefreshListener;
//
//    public void setKRefreshListener(KRefreshListener refreshListener) {
//        mRefreshListener = refreshListener;
//    }
//
//    public void setPinContent(boolean pinContent) {
//        if (mIsPinContent != pinContent) {
//            mIsPinContent = pinContent;
//            if (mIsPinContent && mHeaderView != null) {
//                mHeaderView.getView().bringToFront();
//            }
//        }
//    }
//
//    public void setKeepHeaderWhenRefresh(boolean keep) {
//        mKeepHeaderWhenRefresh = keep;
//    }
//
//
//    public void setHeaderView(KRefreshHeader headerView) {
//        setHeaderView(headerView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
//    }
//
//    public void setHeaderView(KRefreshHeader headerView, int width, int height) {
//        LayoutParams params = generateDefaultLayoutParams();
//        params.width = width;
//        params.height = height;
//        setHeaderView(headerView, params);
//    }
//
//    public void setHeaderView(KRefreshHeader headerView, LayoutParams params) {
//        if (mHeaderView != null) {
//            removeView(mHeaderView.getView());
//        }
//        mHeaderView = headerView;
//        addView(mHeaderView.getView(), params);
//    }
//
//    /**
//     * 自动刷新
//     */
//    public void startRefresh() {
//        if (!mRefreshing && mHeaderView != null) {
//            mRefreshing = true;
//            mIsReset = false;
//            mHeaderView.onRefresh(this);
//            if (mRefreshListener != null) mRefreshListener.onRefresh(this);
//            postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mContentView.scrollTo(0, 0);
//                    animTo(mHeaderView.refreshHeight());
//                }
//            }, 100);
//        }
//    }
//
//    /**
//     * 刷新完成
//     */
//    public void refreshComplete(boolean isSuccess) {
//        if (mRefreshing && mHeaderView != null) {
//            mHeaderView.onComplete(this, isSuccess);
//            if (mCurrentOffset == 0) {
//                mRefreshing = false;
//                mIsReset = true;
//                mHeaderView.onReset(this);
//            } else {
//                //刷新完成停滞时间
//                postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        mRefreshing = false;
//                        animTo(0);
//                    }
//                }, isSuccess ? mHeaderView.succeedRetention() : mHeaderView.failingRetention());
//            }
//        }
//    }
//}
