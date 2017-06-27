package gorden.refresh

/**
 * 加载更多接口
 * 加载方式:
 *      1、滑动到底部自动加载
 *      2、startLoadMore()主动触发加载
 * 默认状态、加载中、加载完成(没有更多数据)、加载失败
 * 滑动过程很平滑的移动过去
 */
interface KLoadMoreFooter {
    /**
     * 加载完成
     * @param hasMore还有更多数据
     */
    fun onComplete(hasMore: Boolean)
    /**
     * 加载失败
     */
    fun onError(refreshLayout: KRefreshLayout)
    /**
     * 加载中
     */
    fun onLoadMore(refreshLayout: KRefreshLayout)
}