package gorden.refresh

import android.animation.ValueAnimator
import android.content.Context
import android.support.v4.view.GestureDetectorCompat
import android.support.v4.view.NestedScrollingChild
import android.support.v4.view.NestedScrollingParent
import android.support.v4.view.ViewCompat
import android.support.v4.widget.NestedScrollView
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.AbsListView
import android.widget.OverScroller

@Suppress("unused", "UNUSED_PARAMETER")
/**
 * Universal pull down the refresh frame
 * version 1.1
 */
class KRefreshLayout(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : ViewGroup(context, attrs, defStyleAttr), NestedScrollingParent {
    private val LOG_TAG = "LOG_KRefreshLayout"

    private var mHeader: KRefreshHeader? = null
    private var mContentView: View? = null

    private var mCurrentOffset: Int = 0
    private var mLastFlingY: Int = 0
    /* 是否处于刷新中 */
    private var mRefreshing = false
    /* 刷新完成后是否已经重置 */
    private var mIsReset = true
    private var mIsBeingDragged = true
    /* 是否处于Fling状态 */
    private var mIsFling = false

    private var nestedScrollExecute = false
    private val MAX_OFFSET = 30//单次最大偏移量
    private var mInitialDownY: Float = 0f
    private var mTouchSlop: Int = 0
    private var mFlingSlop: Int = 2000

    //下拉刷新过程是否钉住contentView
    private var mIsPinContent = false
    //刷新时保持头部
    private var mKeepHeaderWhenRefresh = true
    //收缩动画持续时间
    private var mDurationOffset: Long = 200

    private var mRefreshEnable:Boolean = true

    private var mScroller: OverScroller? = null
    private var mOffsetAnimator: ValueAnimator? = null
    private var mGesture: GestureDetectorCompat? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    init {
        mScroller = OverScroller(context)
        mGesture = GestureDetectorCompat(context, RefreshGestureListener())
        mGesture?.setIsLongpressEnabled(false)
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop * 2

        val a = context.obtainStyledAttributes(attrs, R.styleable.KRefreshLayout)
        mIsPinContent = a.getBoolean(R.styleable.KRefreshLayout_k_pincontent, false)
        mKeepHeaderWhenRefresh = a.getBoolean(R.styleable.KRefreshLayout_k_keepheader, true)
        mDurationOffset = a.getInt(R.styleable.KRefreshLayout_k_durationoffset, 200).toLong()
        a.recycle()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount > 2) {
            throw IllegalStateException("KRefreshLayout111 can only accommodate two elements")
        } else if (childCount == 1) {
            mContentView = getChildAt(0)
        } else if (childCount == 2) {
            val child0 = getChildAt(0)
            if (child0 is KRefreshHeader) {
                mHeader = getChildAt(0) as KRefreshHeader
            }
            mContentView = getChildAt(1)
        }
        mHeader?.getView()?.bringToFront()
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(MATCH_PARENT, MATCH_PARENT)
    }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): LayoutParams {
        return LayoutParams(p)
    }

    @Suppress("unused")
    class LayoutParams : ViewGroup.MarginLayoutParams {
        constructor(c: Context, attrs: AttributeSet) : super(c, attrs)

        constructor(width: Int, height: Int) : super(width, height)

        constructor(source: ViewGroup.MarginLayoutParams) : super(source)

        constructor(source: ViewGroup.LayoutParams) : super(source)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (mHeader != null && !isInEditMode) {
            val lp = mHeader!!.getView().layoutParams as LayoutParams
            val childLeft = paddingLeft + lp.leftMargin
            val childTop = paddingTop + lp.topMargin - mHeader!!.getView().measuredHeight + mCurrentOffset
            val childRight = childLeft + mHeader!!.getView().measuredWidth
            val childBottom = childTop + mHeader!!.getView().measuredHeight
            mHeader!!.getView().layout(childLeft, childTop, childRight, childBottom)
        }

        if (mContentView != null) {
            val lp = mContentView!!.layoutParams as LayoutParams
            val childLeft = paddingLeft + lp.leftMargin
            val childTop = paddingTop + lp.topMargin + if (mIsPinContent) 0 else mCurrentOffset
            val childRight = childLeft + mContentView!!.measuredWidth
            val childBottom = childTop + mContentView!!.measuredHeight
            mContentView!!.layout(childLeft, childTop, childRight, childBottom)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        (0..childCount - 1)
                .map { getChildAt(it) }
                .forEach { measureChildWithMargins(it, widthMeasureSpec, 0, heightMeasureSpec, 0) }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                cancelAnimator()
                mIsFling = false
                mLastFlingY = 0
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                if (!nestedScrollExecute && mCurrentOffset > 0) {
                    finishSpinner()
                }
            }

        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (!isEnabled || mContentView is NestedScrollingChild || nestedScrollExecute || canChildScrollUp() || mHeader == null) {
            return false
        }

        if (!mRefreshEnable) return false

        if (mRefreshing && mIsPinContent && mKeepHeaderWhenRefresh)
            return false

        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                mIsBeingDragged = false
                mInitialDownY = ev.y
                mGesture?.onTouchEvent(ev)
            }
            MotionEvent.ACTION_MOVE -> {
                if (!mIsBeingDragged&&ev.y - mInitialDownY > mTouchSlop){
                    mIsBeingDragged = true
                }

                if (mCurrentOffset>0 && !mIsBeingDragged) {
                    mIsBeingDragged = true
                }
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> mIsBeingDragged = false
        }

        return mIsBeingDragged
    }


    override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        if ((android.os.Build.VERSION.SDK_INT < 21 && mContentView is AbsListView) || !ViewCompat.isNestedScrollingEnabled(mContentView)) {

        } else {
            super.requestDisallowInterceptTouchEvent(disallowIntercept)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled || mHeader == null || nestedScrollExecute || canChildScrollUp())
            return false
        mGesture?.onTouchEvent(event)
        return true
    }

    /**
     * 手势处理
     */
    private inner class RefreshGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            Log.e(LOG_TAG,"RefreshGestureListener  ")
            if ((mCurrentOffset == 0 && distanceY > 0) ||
                    (mCurrentOffset == mHeader!!.maxOffsetHeight() && distanceY < 0))
                return super.onScroll(e1, e2, distanceX, distanceY)
            var offset = -calculateOffset(distanceY.toInt())
            Log.e(LOG_TAG,"RefreshGestureListener  "+offset)
            if (mCurrentOffset + offset > mHeader!!.maxOffsetHeight()) {
                offset = mHeader!!.maxOffsetHeight() - mCurrentOffset
            } else if (mCurrentOffset + offset < 0) {
                offset = -mCurrentOffset
            }
            moveView(offset)
            return super.onScroll(e1, e2, distanceX, distanceY)
        }
    }

    private fun canChildScrollUp(): Boolean {
        return ViewCompat.canScrollVertically(mContentView, -1)
    }

    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
        return isEnabled && mRefreshEnable && mHeader != null && !(mRefreshing && mIsPinContent && mKeepHeaderWhenRefresh)
                && nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int) {
        nestedScrollExecute = false
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        nestedScrollExecute = true
        if (mCurrentOffset > 0 && dy > 0) {
            val offset = if (dy > mCurrentOffset) mCurrentOffset else dy
            consumed[1] = if (dy > mCurrentOffset) dy - mCurrentOffset else dy
            moveView(-offset)
        }
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        var mUnconsumed: Int = dyUnconsumed
        if (dyUnconsumed < 0 && !canChildScrollUp() && mCurrentOffset < mHeader!!.maxOffsetHeight()) {
            if (mCurrentOffset - dyUnconsumed > mHeader!!.maxOffsetHeight()) {
                mUnconsumed = mCurrentOffset - mHeader!!.maxOffsetHeight()
            }
            val offset = -calculateOffset(mUnconsumed)
            moveView(offset)
        }
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        nestedScrollExecute = true

        //如果当前偏移量大于0，则交给KrefreshLayout处理Fling事件
        if (mCurrentOffset > 0) {
            if (velocityY<0&&(!mRefreshing||!mKeepHeaderWhenRefresh||mCurrentOffset>=mHeader!!.refreshHeight())){
                return true
            }
            if (Math.abs(velocityY)>mFlingSlop){
                mIsFling = true
                mScroller?.fling(0, 0, velocityX.toInt(), velocityY.toInt(), Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE)
                invalidate()
            }
            return true
        } else
            return false
    }

    override fun onNestedFling(target: View, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        //如果向上滚动，且处于刷新过程中，监听Fling过程
        if (mRefreshing && velocityY < -mFlingSlop && mKeepHeaderWhenRefresh) {
            mIsFling = true
            mScroller?.fling(0, 0, velocityX.toInt(), velocityY.toInt(), Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE)
            invalidate()
        }
        return true
    }

    override fun onStopNestedScroll(child: View) {
        if (!mIsFling && mCurrentOffset > 0 && nestedScrollExecute) {
            finishSpinner()
        }
        nestedScrollExecute = false
    }


    override fun computeScroll() {
        if (mScroller!!.computeScrollOffset() && mIsFling) {
            //本次Fling移动距离(<0向下滚动、>0向上滚动)
            var offset = mLastFlingY - mScroller!!.currY
            val mFlingMaxHeight = if (offset>0) mHeader!!.refreshHeight() else mHeader!!.maxOffsetHeight()
            //记录上次Fling的Y值
            mLastFlingY = mScroller!!.currY

            if (mCurrentOffset > 0 || offset > 0 && !canChildScrollUp()) {
                offset = if (mCurrentOffset + offset > mFlingMaxHeight) mFlingMaxHeight - mCurrentOffset else if (mCurrentOffset + offset < 0) -mCurrentOffset else offset
                moveView(offset)
                if (mCurrentOffset >= mFlingMaxHeight) {
                    mScroller?.forceFinished(true)
                }
            } else if (offset < 0) {
                if (mContentView is RecyclerView) {
                    (mContentView as RecyclerView).fling(0, mScroller!!.currVelocity.toInt())
                } else if (mContentView is NestedScrollView) {
                    (mContentView as NestedScrollView).fling(mScroller!!.currVelocity.toInt())
                }
                mScroller?.forceFinished(true)
            }
            invalidate()
        } else if (mIsFling) {
            Log.d(LOG_TAG, "mScroll fling complete mCurrentOffset is " + mCurrentOffset)
            mIsFling = false
            if (mCurrentOffset > 0)
                finishSpinner()
        }
    }


    /**
     * 计算实际偏移量
     */
    private fun calculateOffset(offset: Int): Int {
        val mOffset: Int
        val downResistance: Float
        if (offset > 0) {
            downResistance = 0.8f
        } else {
            //下拉阻力(0f-1f) 越小阻力越大，当前计算公式:1-mCurrentOffset/maxheight
            downResistance = 1f - mCurrentOffset.toFloat() / mHeader!!.maxOffsetHeight()
        }
        if (offset > 0) {
            mOffset = Math.min(MAX_OFFSET, Math.ceil((downResistance * offset).toDouble()).toInt())
        } else {
            mOffset = Math.max(-MAX_OFFSET, Math.floor((downResistance * offset).toDouble()).toInt())
        }
        return mOffset
    }

    /**
     * 移动视图

     * @param offset 偏移量
     * *               //     * @param requiresUpdate 是否需要更新
     */
    private fun moveView(offset: Int) {
        var invalidate = false
        if (!mRefreshing && mCurrentOffset == 0 && offset > 0) {
            mHeader?.onPrepare(this)
            invalidate = true
        }

        if (mCurrentOffset > height) {
            invalidate = true
        }

        mCurrentOffset += offset
        mHeader!!.getView().offsetTopAndBottom(offset)
        if (!mIsPinContent)
            mContentView?.offsetTopAndBottom(offset)
        if (invalidate) invalidate()
        mHeader?.onScroll(this, mCurrentOffset, mCurrentOffset.toFloat() / mHeader!!.refreshHeight(), mRefreshing)
        mScrollListener?.onScroll(offset,mCurrentOffset,mCurrentOffset.toFloat() / mHeader!!.refreshHeight(), mRefreshing)

        if (!mRefreshing && offset < 0 && mCurrentOffset == 0) {
            mHeader?.onReset(this)
            mIsReset = true
        }
    }

    /**
     * 取消offset动画
     */
    private fun cancelAnimator() {
        if (mOffsetAnimator != null && mOffsetAnimator!!.isRunning) {
            mOffsetAnimator?.cancel()
        }
    }

    /**
     * offset动画更新监听
     */
    private val mAnimatorUpdateListener = ValueAnimator.AnimatorUpdateListener { animation ->
        val value = animation.animatedValue as Int
        moveView(value - mCurrentOffset)
    }

    /**
     * 结束下拉
     */
    private fun finishSpinner() {
        Log.d(LOG_TAG, "finishSpinner mCurrentOffset is $mCurrentOffset , mRefreshing is $mRefreshing")
        val target: Int
        if (mRefreshing) {
            target = if (mCurrentOffset >= mHeader!!.refreshHeight() / 2) mHeader!!.refreshHeight() else 0
        } else {
            target = if (mCurrentOffset >= mHeader!!.refreshHeight() && mIsReset) mHeader!!.refreshHeight() else 0
            if (mCurrentOffset >= mHeader!!.refreshHeight() && mIsReset) {
                mRefreshing = true//开始刷新
                mIsReset = false
                mHeader?.onRefresh(this)
                mRefreshListener?.onRefresh(this)
            }
        }
        animTo(target)
    }


    /**
     * 动画方式移动

     * @param target 目标位置
     */
    private fun animTo(target: Int) {
        if (mOffsetAnimator == null) {
            mOffsetAnimator = ValueAnimator()
            mOffsetAnimator?.addUpdateListener(mAnimatorUpdateListener)
        }

        cancelAnimator()

        if (mCurrentOffset == target) {
            return
        }
        val mTarget: Int = if (mKeepHeaderWhenRefresh) target else 0

        Log.d(LOG_TAG, "animTo $mCurrentOffset to $mTarget")
        mOffsetAnimator?.duration = mDurationOffset
        mOffsetAnimator?.setIntValues(mCurrentOffset, mTarget)
        mOffsetAnimator?.start()
    }


    private var mRefreshListener: KRefreshListener? = null

    fun setKRefreshListener(refreshListener: KRefreshListener) {
        mRefreshListener = refreshListener
    }

    fun setKRefreshListener(refreshListener: (KRefreshLayout) -> Unit): Unit {
        mRefreshListener = object : KRefreshListener {
            override fun onRefresh(refreshLayout: KRefreshLayout) {
                refreshListener(refreshLayout)
            }
        }
    }

    interface KRefreshListener {
        fun onRefresh(refreshLayout: KRefreshLayout)
    }

    private var mScrollListener: KScrollListener? = null

    fun setKScrollListener(scrollListener: KScrollListener) {
        mScrollListener = scrollListener
    }

    fun setKScrollListener(scrollListener: (offset: Int, distance: Int, percent: Float, refreshing: Boolean) -> Unit): Unit {
        mScrollListener = object : KScrollListener {
            override fun onScroll(offset: Int, distance: Int, percent: Float, refreshing: Boolean) {
                scrollListener(offset,distance,percent,refreshing)
            }
        }
    }

    interface KScrollListener{
        /**
         * @param offset 本次的偏移量
         * @param distance 总的偏移量
         * @param percent 偏移比率
         * @param refreshing 是否在刷新
         */
        fun onScroll(offset: Int, distance: Int, percent: Float, refreshing: Boolean)
    }

    fun setKeepHeaderWhenRefresh(keep: Boolean) {
        mKeepHeaderWhenRefresh = keep
    }

    fun setRefreshEnable(enable:Boolean){
        mRefreshEnable = enable
    }

    fun setPinContent(pinContent: Boolean) {
        mIsPinContent = pinContent
    }

    fun setDurationOffset(duration: Long) {
        mDurationOffset = duration
    }

    fun setHeaderView(headerView: KRefreshHeader) {
        setHeaderView(headerView, MATCH_PARENT, WRAP_CONTENT)
    }

    fun setHeaderView(headerView: KRefreshHeader, width: Int, height: Int) {
        val params = generateDefaultLayoutParams()
        params.width = width
        params.height = height
        setHeaderView(headerView, params)
    }

    fun setHeaderView(headerView: KRefreshHeader, params: LayoutParams) {
        if (mHeader != null) {
            removeView(mHeader?.getView())
        }
        mHeader = headerView
        addView(mHeader?.getView(), 0, params)
        mHeader?.getView()?.bringToFront()
    }

    /**
     * 自动刷新
     */
    fun startRefresh() {
        if (!mRefreshing && mHeader != null) {
            mRefreshing = true
            mIsReset = false
            mHeader?.onRefresh(this)
            mRefreshListener?.onRefresh(this)
            postDelayed({
                mContentView?.scrollTo(0, 0)
                animTo(mHeader!!.refreshHeight())
            }, 100)
        }
    }

    /**
     * 刷新完成
     */
    fun refreshComplete(isSuccess: Boolean) {
        if (mRefreshing && mHeader != null) {
            mHeader?.onComplete(this, isSuccess)
            if (mCurrentOffset == 0) {
                mRefreshing = false
                mIsReset = true
                mHeader?.onReset(this)
            } else {
                //刷新完成停滞时间
                mRefreshing = false
                postDelayed({
                    animTo(0)
                }, if (isSuccess) mHeader!!.succeedRetention() else mHeader!!.failingRetention())
            }
        }
    }
}