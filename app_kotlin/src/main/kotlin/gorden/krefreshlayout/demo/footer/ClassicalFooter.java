package gorden.krefreshlayout.demo.footer;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import gorden.krefreshlayout.demo.R;
import gorden.krefreshlayout.demo.util.DensityUtil;
import gorden.krefreshlayout.demo.widget.recyclerview.KLoadMoreView;
import gorden.krefreshlayout.demo.widget.recyclerview.KRecyclerView;


/**
 * 经典下拉刷新
 * Created by Gorden on 2017/6/17.
 */

public class ClassicalFooter extends FrameLayout implements KLoadMoreView {
    private KRecyclerView recyclerView;
    private ImageView arrawImg;
    private TextView textTitle;

    private RotateAnimation rotateAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

    public ClassicalFooter(@NonNull Context context) {
        this(context, null);
    }

    public ClassicalFooter(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClassicalFooter(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setGravity(Gravity.CENTER_VERTICAL);
        addView(root, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        ((LayoutParams) root.getLayoutParams()).gravity = Gravity.CENTER;

        arrawImg = new ImageView(context);
        arrawImg.setImageResource(R.drawable.ic_loading);
        arrawImg.setScaleType(ImageView.ScaleType.CENTER);
        root.addView(arrawImg);

        textTitle = new TextView(context);
        textTitle.setTextSize(13);
        textTitle.setText("上拉或点击加载更多...");
        textTitle.setTextColor(Color.parseColor("#999999"));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.leftMargin = 20;
        root.addView(textTitle, params);

        rotateAnimation.setDuration(800);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        rotateAnimation.setRepeatMode(Animation.RESTART);
        setPadding(0, DensityUtil.dip2px(15), 0, DensityUtil.dip2px(15));

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.startLoadMore();
            }
        });
    }

    @Override
    public boolean shouldLoadMore(@NotNull KRecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        return false;
    }

    @Override
    public void onLoadMore(@NotNull KRecyclerView recyclerView) {
        arrawImg.setVisibility(VISIBLE);
        arrawImg.startAnimation(rotateAnimation);
        textTitle.setText("正在加载...");
    }

    @Override
    public void onComplete(@NotNull KRecyclerView recyclerView, boolean hasMore) {
        arrawImg.clearAnimation();
        textTitle.setText(hasMore ? "上拉或点击加载更多..." : "没有更多数据");
        arrawImg.setVisibility(GONE);
    }

    @Override
    public void onError(@NotNull KRecyclerView recyclerView, int errorCode) {
        arrawImg.clearAnimation();
        textTitle.setText("加载失败,点击重新加载");
        arrawImg.setVisibility(GONE);
    }
}
