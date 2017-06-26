package gorden.krefreshlayout.demo.header.circle;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import org.jetbrains.annotations.NotNull;

import gorden.krefreshlayout.demo.util.DensityUtil;
import gorden.refresh.KRefreshHeader;
import gorden.refresh.KRefreshLayout;

/**
 * document
 * Created by Gordn on 2017/6/22.
 */

public class CircleHeader extends FrameLayout implements KRefreshHeader {
    AnimationView mHeader;
    private ValueAnimator mUpTopAnimator;
    public CircleHeader(@NonNull Context context) {
        super(context);
        mHeader = new AnimationView(context);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
        params.gravity = Gravity.TOP;
        mHeader.setLayoutParams(params);
        addView(mHeader);

        mHeader.setAniBackColor(0xff8b90af);
        mHeader.setAniForeColor(0xffffffff);
        mHeader.setRadius(7);

        mHeader.setOnViewAniDone(new AnimationView.OnViewAniDone() {
            @Override
            public void viewAniDone() {
                mUpTopAnimator.start();
            }
        });

        mUpTopAnimator = ValueAnimator.ofFloat(refreshHeight(), 0);
        mUpTopAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float val = (float) animation.getAnimatedValue();
                mHeader.getLayoutParams().height = (int) val;
                mHeader.requestLayout();
            }
        });
        mUpTopAnimator.setDuration(200);
    }
    @Override
    public long succeedRetention() {
        return 1000;
    }

    @Override
    public long failingRetention() {
        return 0;
    }

    @Override
    public int refreshHeight() {
        return DensityUtil.dip2px(100);
    }

    @Override
    public int maxOffsetHeight() {
        return DensityUtil.dip2px(150);
    }

    @Override
    public void onReset(@NotNull KRefreshLayout refreshLayout) {

    }

    @Override
    public void onPrepare(@NotNull KRefreshLayout refreshLayout) {
    }

    @Override
    public void onRefresh(@NotNull KRefreshLayout refreshLayout) {
        mHeader.releaseDrag();
    }

    @Override
    public void onComplete(@NotNull KRefreshLayout refreshLayout, boolean isSuccess) {
        mHeader.setRefreshing(false);
    }

    @Override
    public void onScroll(@NotNull KRefreshLayout refreshLayout, int distance, float percent, boolean refreshing) {
        if (!refreshing){
            mHeader.getLayoutParams().height = distance;
            mHeader.requestLayout();
        }
    }
}
