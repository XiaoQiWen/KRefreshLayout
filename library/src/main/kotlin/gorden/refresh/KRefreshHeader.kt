package gorden.refresh

import android.view.View

interface KRefreshHeader {
    /**
     * 返回headerView
     */
    fun getView(): View

    /**
     * 刷新成功停滞时间
     */
    fun succeedRetention(): Long
    /**
     * 刷新失败停滞时间
     */
    fun failingRetention(): Long

    /**
     * 刷新高度，达到这个高度将触发刷新
     */
    fun refreshHeight(): Int

    /**
     * Content最大下拉高度,无特殊要求
     */
    fun maxOffsetHeight(): Int

    /**
     *  当内容视图到达顶部，视图将被重置
     */
    fun onReset(refreshLayout: KRefreshLayout)

    /**
     * 准备刷新，作初始化工作
     */
    fun onPrepare(refreshLayout: KRefreshLayout)

    /**
     * 刷新中
     */
    fun onRefresh(refreshLayout: KRefreshLayout)

    /**
     * 刷新完成
     */
    fun onComplete(refreshLayout: KRefreshLayout,isSuccess:Boolean)

    /**
     * 拖拽中
     * @param distance 当前滑动距离
     * @param percent 当前移动比例  0f - max  1.0为刷新临界点
     * @param refreshing 是否处于刷新中
     */
    fun onScroll(refreshLayout: KRefreshLayout, distance: Int, percent: Float, refreshing: Boolean)

}