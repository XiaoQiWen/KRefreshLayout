package gorden.refresh;


import android.view.View;

public interface JRefreshHeader {
    View getView();
    /**
     * 刷新成功停滞时间
     */
    long succeedRetention();
    /**
     * 刷新失败停滞时间
     */
    long failingRetention();
    /**
     * 刷新高度，达到这个高度将触发刷新
     */
    int refreshHeight();
    /**
     * 最大下拉高度
     */
    int maxOffsetHeight();

    /**
     *  当内容视图到达顶部，视图将被重置
     */
    void onReset(JRefreshLayout refreshLayout);
    /**
     * 准备下拉，作初始化工作
     */
    void onPrepare(JRefreshLayout refreshLayout);
    /**
     * 刷新中
     */
    void onRefresh(JRefreshLayout refreshLayout);
    /**
     * 刷新完成
     * @param isSuccess 成功还是失败
     */
    void onComplete(JRefreshLayout refreshLayout,boolean isSuccess);
    /**
     * 拖拽中
     * @param distance 当前滑动距离
     * @param percent 当前移动比例  0f - max  1.0为刷新临界点
     * @param refreshing 是否处于刷新中
     */
    void onScroll(JRefreshLayout refreshLayout,int distance,float percent,boolean refreshing);
}
