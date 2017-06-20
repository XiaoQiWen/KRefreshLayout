package gorden.krefreshlayout.demo.header.materia;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.jetbrains.annotations.NotNull;

import gorden.krefreshlayout.demo.R;
import gorden.refresh.KRefreshHeader;
import gorden.refresh.KRefreshLayout;

/**
 * document
 * Created by Gordn on 2017/6/20.
 */

public class MateriaProgressHeader extends FrameLayout implements KRefreshHeader{

    private int mCircleWidth;
    private int mCircleHeight;
    private static final int CIRCLE_DIAMETER = 40;
    private static final int CIRCLE_DIAMETER_LARGE = 56;
    // Maps to ProgressBar.Large style
    public static final int LARGE = MaterialProgressDrawable.LARGE;
    // Maps to ProgressBar default style
    public static final int DEFAULT = MaterialProgressDrawable.DEFAULT;
    // Default background for the progress spinner
    private static final int CIRCLE_BG_LIGHT = 0xFFFAFAFA;
    // Default offset in dips from the top of the view to where the progress spinner should stop
    private static final int DEFAULT_CIRCLE_TARGET = 64;
    private static final float MAX_PROGRESS_ANGLE = .8f;

    private static final int MAX_ALPHA = 255;
    private static final int STARTING_PROGRESS_ALPHA = (int) (.3f * MAX_ALPHA);

    private CircleImageView mCircleView;
    private MaterialProgressDrawable mProgress;

    public MateriaProgressHeader(@NonNull Context context) {
        this(context,null);
    }

    public MateriaProgressHeader(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        mCircleWidth = (int) (CIRCLE_DIAMETER * metrics.density);
        mCircleHeight = (int) (CIRCLE_DIAMETER * metrics.density);
        createProgressView();
        ViewCompat.setChildrenDrawingOrderEnabled(this, true);

        setColorSchemeColors(getResources().getIntArray(R.array.refresh_color));

    }
    private void createProgressView() {
        mCircleView = new CircleImageView(getContext(), CIRCLE_BG_LIGHT, CIRCLE_DIAMETER / 2);
        mProgress = new MaterialProgressDrawable(getContext(), this);
        mProgress.setBackgroundColor(CIRCLE_BG_LIGHT);
        mCircleView.setImageDrawable(mProgress);
        mCircleView.setVisibility(View.GONE);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        mCircleView.setLayoutParams(params);
        addView(mCircleView);
    }

    /**
     * Set the background color of the progress spinner disc.
     *
     * @param color
     */
    public void setProgressBackgroundColorSchemeColor(@ColorInt int color) {
        mCircleView.setBackgroundColor(color);
        mProgress.setBackgroundColor(color);
    }

    public void setColorSchemeResources(@ColorRes int... colorResIds) {
        final Resources res = getResources();
        int[] colorRes = new int[colorResIds.length];
        for (int i = 0; i < colorResIds.length; i++) {
            colorRes[i] = res.getColor(colorResIds[i]);
        }
        setColorSchemeColors(colorRes);
    }

    public void setColorSchemeColors(int... colors) {
        mProgress.setColorSchemeColors(colors);
    }

    public void setSize(int size) {
        if (size != MaterialProgressDrawable.LARGE && size != MaterialProgressDrawable.DEFAULT) {
            return;
        }
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        if (size == MaterialProgressDrawable.LARGE) {
            mCircleHeight = mCircleWidth = (int) (CIRCLE_DIAMETER_LARGE * metrics.density);
        } else {
            mCircleHeight = mCircleWidth = (int) (CIRCLE_DIAMETER * metrics.density);
        }
        // force the bounds of the progress circle inside the circle view to
        // update by setting it to null before updating its size and then
        // re-setting it
        mCircleView.setImageDrawable(null);
        mProgress.updateSizes(size);
        mCircleView.setImageDrawable(mProgress);
    }

    @NotNull
    @Override
    public View getView() {
        return this;
    }

    @Override
    public long succeedRetention() {
        return 200;
    }

    @Override
    public long failingRetention() {
        return 0;
    }

    @Override
    public int refreshHeight() {
        return getHeight();
    }

    @Override
    public int maxOffsetHeight() {
        return 2*getHeight();
    }
    boolean isReset = true;
    @Override
    public void onReset(@NotNull KRefreshLayout refreshLayout) {
        mCircleView.clearAnimation();
        mProgress.stop();
        mCircleView.setVisibility(View.GONE);

        mCircleView.getBackground().setAlpha(MAX_ALPHA);
        mProgress.setAlpha(MAX_ALPHA);
        ViewCompat.setScaleX(mCircleView, 0);
        ViewCompat.setScaleY(mCircleView, 0);
        ViewCompat.setAlpha(mCircleView, 1);
        isReset = true;
    }

    @Override
    public void onPrepare(@NotNull KRefreshLayout refreshLayout) {
        mProgress.setAlpha(STARTING_PROGRESS_ALPHA);
    }

    @Override
    public void onRefresh(@NotNull KRefreshLayout refreshLayout) {
        mCircleView.setVisibility(View.VISIBLE);
        mCircleView.getBackground().setAlpha(MAX_ALPHA);
        mProgress.setAlpha(MAX_ALPHA);
        ViewCompat.setScaleX(mCircleView, 1f);
        ViewCompat.setScaleY(mCircleView, 1f);
        mProgress.setArrowScale(1f);
        mProgress.start();
        isReset = false;
    }

    @Override
    public void onComplete(@NotNull KRefreshLayout refreshLayout, boolean isSuccess) {
        mCircleView.animate().scaleX(0).scaleY(0).alpha(0).start();
    }

    @Override
    public void onScroll(@NotNull KRefreshLayout refreshLayout, int distance, float percent, boolean refreshing) {

        if (!refreshing&&isReset){
            if (mCircleView.getVisibility() != View.VISIBLE) {
                mCircleView.setVisibility(View.VISIBLE);
            }

            if (percent >= 1f) {
                ViewCompat.setScaleX(mCircleView, 1f);
                ViewCompat.setScaleY(mCircleView, 1f);
            } else {
                ViewCompat.setScaleX(mCircleView, percent);
                ViewCompat.setScaleY(mCircleView, percent);
            }

            if (percent <= 1f) {
                mProgress.setAlpha((int) (STARTING_PROGRESS_ALPHA + (MAX_ALPHA - STARTING_PROGRESS_ALPHA) * percent));
            }

            float adjustedPercent = (float) Math.max(percent - .4, 0) * 5 / 3;
            float strokeStart = adjustedPercent * .8f;
            mProgress.setStartEndTrim(0f, Math.min(MAX_PROGRESS_ANGLE, strokeStart));
            mProgress.setArrowScale(Math.min(1f, adjustedPercent));
            float rotation = (-0.25f + .4f * adjustedPercent) * .5f;
            mProgress.setProgressRotation(rotation);
        }
    }
}
