package gorden.krefreshlayout.demo.header;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.jetbrains.annotations.NotNull;

import gorden.krefreshlayout.demo.R;
import gorden.krefreshlayout.demo.util.DensityUtil;
import gorden.refresh.KRefreshHeader;
import gorden.refresh.KRefreshLayout;

/**
 * document
 * Created by Gordn on 2017/6/26.
 */

public class WechatHeader extends FrameLayout implements KRefreshHeader {
    private ImageView imgChat;
    private RotateAnimation rotateAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
    private ValueAnimator returnAnima = new ValueAnimator();

    public WechatHeader(@NonNull Context context) {
        this(context, null);
    }

    public WechatHeader(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        imgChat = new ImageView(context);
        imgChat.setImageResource(R.drawable.ic_wechat);
        LayoutParams params = new LayoutParams(DensityUtil.dip2px(30), DensityUtil.dip2px(30));
        params.leftMargin = DensityUtil.dip2px(20);
        addView(imgChat, params);

        rotateAnimation.setDuration(800);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        rotateAnimation.setRepeatMode(Animation.RESTART);

        returnAnima.setDuration(800);
        returnAnima.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int progress = (int) animation.getAnimatedValue();
                offsetTopAndBottom(progress - mDistance);
                imgChat.setRotation(progress);
                mDistance = progress;
                if (getParent() instanceof KRefreshLayout) {
                    ((KRefreshLayout) getParent()).setHeaderOffset(mDistance - lastDistance);
                }
            }
        });
    }

    @Override
    public long succeedRetention() {
        return 0;
    }

    @Override
    public long failingRetention() {
        return 0;
    }

    @Override
    public int refreshHeight() {
        return DensityUtil.dip2px(50);
    }

    @Override
    public int maxOffsetHeight() {
        return ((View) getParent()).getHeight();
    }

    @Override
    public void onReset(@NotNull KRefreshLayout refreshLayout) {

    }

    @Override
    public void onPrepare(@NotNull KRefreshLayout refreshLayout) {
    }

    @Override
    public void onRefresh(@NotNull KRefreshLayout refreshLayout) {
        imgChat.startAnimation(rotateAnimation);
    }

    @Override
    public void onComplete(@NotNull KRefreshLayout refreshLayout, boolean isSuccess) {
        imgChat.clearAnimation();
        returnAnima.setIntValues(mDistance, 0);
        returnAnima.start();
    }

    private int mDistance = 0;
    private int lastDistance;

    @Override
    public void onScroll(@NotNull KRefreshLayout refreshLayout, int distance, float percent, boolean refreshing) {
        int offset = distance - lastDistance;
        if (returnAnima.isRunning())
            returnAnima.cancel();
        lastDistance = distance;

        if (!refreshing) {
            imgChat.setRotation(-distance);
            if (percent > 1) {
                offsetTopAndBottom(-offset);
                if (mDistance != refreshHeight()) {
                    offset = refreshHeight() - mDistance;
                    offsetTopAndBottom(offset);
                    mDistance += offset;
                }
            } else {
                if (mDistance + offset != distance) {
                    offset = distance - (mDistance + offset);
                    offsetTopAndBottom(offset);
                }
                mDistance = distance;
            }
        } else {
            offsetTopAndBottom(-offset);
        }
        refreshLayout.setHeaderOffset(mDistance - distance);
    }
}
