package gorden.krefreshlayout.demo.header.rentals;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import org.jetbrains.annotations.NotNull;

import gorden.krefreshlayout.demo.util.DensityUtil;
import gorden.refresh.KRefreshHeader;
import gorden.refresh.KRefreshLayout;

/*
 * 一个经典的太阳升起刷新Header
 */

public class RentalsSunHeaderView extends View implements KRefreshHeader {
    private RentalsSunDrawable mDrawable;
    private float mPercent;
    private int mDistance;

    public RentalsSunHeaderView(Context context) {
        super(context);
        init();
    }

    public RentalsSunHeaderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RentalsSunHeaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mDrawable = new RentalsSunDrawable(getContext(), this);
        setPadding(0, DensityUtil.dip2px(15), 0, DensityUtil.dip2px(10));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = mDrawable.getTotalDragDistance() * 5 / 4;
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height + getPaddingTop() + getPaddingBottom(), MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int pl = getPaddingLeft();
        int pt = getPaddingTop();
        mDrawable.setBounds(pl, pt, pl + right - left, pt + bottom - top);
    }

    @Override
    public long succeedRetention() {
        return 300;
    }

    @Override
    public long failingRetention() {
        return 0;
    }

    @Override
    public int refreshHeight() {
        return mDrawable.getTotalDragDistance()+DensityUtil.dip2px(15);
    }

    @Override
    public int maxOffsetHeight() {
        return getHeight();
    }

    @Override
    public void onReset(@NotNull KRefreshLayout refreshLayout) {
        mDrawable.resetOriginals();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mDrawable.draw(canvas);
    }

    @Override
    public void onPrepare(@NotNull KRefreshLayout refreshLayout) {
    }

    @Override
    public void onRefresh(@NotNull KRefreshLayout refreshLayout) {
        mDrawable.start();
        mDrawable.offsetTopAndBottom(mDistance);
        mDrawable.setPercent(mPercent);
        invalidate();
    }

    @Override
    public void onComplete(@NotNull KRefreshLayout refreshLayout,boolean isSuccess) {
        mDrawable.stop();
        mDrawable.offsetTopAndBottom(mDistance);
        mDrawable.setPercent(mPercent);
        invalidate();
    }


    @Override
    public int onScroll(@NotNull KRefreshLayout refreshLayout, int distance, float percent, boolean refreshing) {
        mPercent = percent;
        mDistance = distance;
        mDrawable.offsetTopAndBottom(distance);
        mDrawable.setPercent(percent);
        invalidate();
        return 0;
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable drawable) {
        if (drawable == mDrawable) {
            invalidate();
        } else {
            super.invalidateDrawable(drawable);
        }
    }
}
