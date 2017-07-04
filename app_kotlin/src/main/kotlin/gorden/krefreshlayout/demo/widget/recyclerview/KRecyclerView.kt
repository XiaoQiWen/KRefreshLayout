package gorden.krefreshlayout.demo.widget.recyclerview

import android.content.Context
import android.support.v4.util.SparseArrayCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

@Suppress("unused")
/**
 * 支持添加Header Footer
 * 支持加载更多
 */
class KRecyclerView(context: Context, attrs: AttributeSet?, defStyle: Int) : RecyclerView(context, attrs, defStyle) {
    private val ITEM_TYPE_HEADER_INIT = 100000
    private val ITEM_TYPE_FOOTER_INIT = 200000
    private val ITEM_TYPE_LOADMORE = 300000

    var hasMore: Boolean = false
    private var mLoading: Boolean = false
    var loadMoreEnable: Boolean = true

    private var mWrapAdapter: WrapAdapter? = null

    private var mLoadMoreView: KLoadMoreView? = null
    private val mHeaderViews = SparseArrayCompat<View>()
    private val mFooterViews = SparseArrayCompat<View>()

    private var mLoadMoreListener: ((RecyclerView) -> Unit)? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    override fun setAdapter(adapter: Adapter<ViewHolder>?) {
        mWrapAdapter = WrapAdapter(adapter)
        super.setAdapter(mWrapAdapter)

        if (!(adapter?.hasObservers() ?: true)) {
            adapter?.registerAdapterDataObserver(KDataObserver())
        }
    }


    /**
     * 添加HeaderView
     */
    fun addHeaderView(view: View) {
        mHeaderViews.put(mHeaderViews.size() + ITEM_TYPE_HEADER_INIT, view)
        mWrapAdapter?.notifyDataSetChanged()
    }

    /**
     * 添加FooterView
     */
    fun addFooterView(view: View) {
        mFooterViews.put(mFooterViews.size() + ITEM_TYPE_FOOTER_INIT, view)
        mWrapAdapter?.notifyDataSetChanged()
    }

    /**
     * 设置LoadMoreView
     * 必须是一个视图View
     */
    fun setLoadMoreView(loadMoreView: KLoadMoreView) {
        if (loadMoreView !is View) {
            throw IllegalStateException("KLoadMoreView must is a View?")
        }

        this.mLoadMoreView = loadMoreView

        mWrapAdapter?.notifyDataSetChanged()

        removeOnScrollListener(defaultScrollListener)
        if (!(mLoadMoreView?.shouldLoadMore(this) ?: true)) {
            addOnScrollListener(defaultScrollListener)
        }
    }

    /**
     * 开始加载更多
     */
    fun startLoadMore() {
        if (!mLoading && loadMoreEnable && hasMore) {
            mLoading = true
            mLoadMoreView?.onLoadMore(this)
            mLoadMoreListener?.invoke(this)
        }
    }

    /**
     * 加载完成
     */
    fun loadMoreComplete(hasMore: Boolean) {
        mLoading = false
        mLoadMoreView?.onComplete(this, hasMore)
        this.hasMore = hasMore
    }

    fun loadMoreError(errorCode:Int){
        mLoading = false
        mLoadMoreView?.onError(this,errorCode)
    }

    /**
     * 设置加载更多监听
     */
    fun setLoadMoreListener(loadMoreListener: (recyclerView: RecyclerView) -> Unit): Unit {
        mLoadMoreListener = loadMoreListener
    }

    /**
     * 默认的加载触发时机
     */
    private val defaultScrollListener = object : OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (!recyclerView.canScrollVertically(1)) {
                startLoadMore()
            }
        }
    }

    private inner class WrapAdapter(var adapter: Adapter<ViewHolder>?) : Adapter<ViewHolder>() {
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (!isContent(position)) {
                return
            }
            adapter?.onBindViewHolder(holder, position - mHeaderViews.size())
        }

        override fun getItemCount(): Int {
            val adapterCount = adapter?.itemCount ?: 0

            if (adapterCount > 0) {
                return adapterCount + mHeaderViews.size() + mFooterViews.size() + if (mLoadMoreView == null) 0 else 1
            } else {//防止没有内容的时候加载更多显示出来
                return mHeaderViews.size() + mFooterViews.size()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder? {
            if (mHeaderViews[viewType] != null) {
                return object : ViewHolder(mHeaderViews[viewType]) {
                    init {
                        itemView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                    }
                }
            }
            if (viewType == ITEM_TYPE_LOADMORE)
                return object : ViewHolder(mLoadMoreView as View) {
                    init {
                        itemView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                    }
                }

            if (mFooterViews[viewType] != null) {
                return object : ViewHolder(mFooterViews[viewType]) {
                    init {
                        itemView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                    }
                }
            }
            return adapter?.onCreateViewHolder(parent, viewType)
        }

        override fun getItemViewType(position: Int): Int {
            if (position < mHeaderViews.size()) {
                return mHeaderViews.keyAt(position)
            }

            if (mLoadMoreView != null && position == itemCount - 1) {
                return ITEM_TYPE_LOADMORE
            }

            if (position >= mHeaderViews.size() + (adapter?.itemCount ?: 0)) {
                return mFooterViews.keyAt(position - mHeaderViews.size() - (adapter?.itemCount ?: 0))
            }

            return adapter?.getItemViewType(position-mHeaderViews.size()) ?: -1
        }

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
            adapter?.onAttachedToRecyclerView(recyclerView)
            val layoutManager = layoutManager
            if (layoutManager is GridLayoutManager) {
                layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        if (!isContent(position)) {
                            return layoutManager.spanCount
                        }
                        return 1
                    }
                }
            }
        }

        override fun onViewAttachedToWindow(holder: ViewHolder) {
            adapter?.onViewAttachedToWindow(holder)
            val position = holder.layoutPosition
            val layoutParams = holder.itemView.layoutParams
            if (!isContent(position) && layoutParams != null && layoutParams is StaggeredGridLayoutManager.LayoutParams) {
                layoutParams.isFullSpan = true
            }
        }

        fun isContent(position: Int): Boolean {
            if (position < mHeaderViews.size())
                return false
            if (mLoadMoreView != null && position == itemCount - 1)
                return false
            if (position >= mHeaderViews.size() + (adapter?.itemCount ?: 0))
                return false
            return true
        }

    }

    private inner class KDataObserver : AdapterDataObserver() {
        override fun onChanged() {
            mWrapAdapter?.notifyDataSetChanged()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            mWrapAdapter?.notifyItemRangeChanged(positionStart, itemCount)
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            mWrapAdapter?.notifyItemRangeChanged(positionStart, itemCount, payload)
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            mWrapAdapter?.notifyItemRangeInserted(positionStart, itemCount)
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            mWrapAdapter?.notifyItemMoved(fromPosition, toPosition)
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            mWrapAdapter?.notifyItemRangeRemoved(positionStart, itemCount)
        }
    }
}