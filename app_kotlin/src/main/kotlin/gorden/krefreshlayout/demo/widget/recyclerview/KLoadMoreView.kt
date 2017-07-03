package gorden.krefreshlayout.demo.widget.recyclerview

interface KLoadMoreView {
    /**
     * 自定义适当的加载时机
     * @return true 自定义生效 false默认的加载时机
     */
    fun shouldLoadMore(recyclerView: KRecyclerView):Boolean

    /**
     * 正在加载
     */
    fun onLoadMore(recyclerView: KRecyclerView)

    /**
     * 加载完成
     * @param hasMore 是否还有更多数据
     */
    fun onComplete(recyclerView: KRecyclerView, hasMore:Boolean)

    /**
     * 加载失败
     * @param errorCode 错误码，由用户定义
     */
    fun onError(recyclerView: KRecyclerView, errorCode:Int)
}