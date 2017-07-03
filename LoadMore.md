#### 为什么不将LoadMore功能集成进RefreshLayout
* 实现效果不友好,loadMoreView加载完成后,会有个回弹效果（个人不喜欢）
* 加载更多的触发时机不好控制（比如滚动到内容倒数几条的时候自动加载）
---
[可以参考Demo基于RecyclerView实现的加载更多](https://github.com/XiaoQiWen/KRefreshLayout/tree/master/app_kotlin/src/main/kotlin/gorden/krefreshlayout/demo/widget/recyclerview)   [LoadMoreFragment](https://github.com/XiaoQiWen/KRefreshLayout/blob/master/app_kotlin/src/main/kotlin/gorden/krefreshlayout/demo/ui/fragment/SampleJFragment.kt)</br></br>
![img](https://github.com/XiaoQiWen/Resources/raw/master/KRefreshLayout/gif6.gif)</br>
Demo中[KLoadMoreView](https://github.com/XiaoQiWen/KRefreshLayout/blob/master/app_kotlin/src/main/kotlin/gorden/krefreshlayout/demo/widget/recyclerview/KLoadMoreView.kt)说明
```
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
```
