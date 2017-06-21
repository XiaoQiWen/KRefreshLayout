package gorden.krefreshlayout.demo.header;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import org.jetbrains.annotations.NotNull;

import gorden.refresh.KRefreshHeader;
import gorden.refresh.KRefreshLayout;

/**
 * document
 * Created by Gordn on 2017/6/21.
 */

public class NullHeader extends FrameLayout implements KRefreshHeader{
    public NullHeader(@NonNull Context context) {
        super(context);
    }

    public NullHeader(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NullHeader(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @NotNull
    @Override
    public View getView() {
        return this;
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
        return 301;
    }

    @Override
    public int maxOffsetHeight() {
        return 300;
    }

    @Override
    public void onReset(@NotNull KRefreshLayout refreshLayout) {

    }

    @Override
    public void onPrepare(@NotNull KRefreshLayout refreshLayout) {

    }

    @Override
    public void onRefresh(@NotNull KRefreshLayout refreshLayout) {

    }

    @Override
    public void onComplete(@NotNull KRefreshLayout refreshLayout, boolean isSuccess) {

    }

    @Override
    public void onScroll(@NotNull KRefreshLayout refreshLayout, int distance, float percent, boolean refreshing) {

    }
}
