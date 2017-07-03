package gorden.krefreshlayout.demo.ui.fragment

import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import gorden.krefreshlayout.demo.R
import gorden.krefreshlayout.demo.footer.ClassicalFooter
import gorden.krefreshlayout.demo.header.ClassicalHeader
import gorden.refresh.KRefreshLayout
import kotlinx.android.synthetic.main.layout_krecyclerview.*

/**
 * document
 * Created by Gordn on 2017/6/21.
 */
class SampleJFragment : ISampleFragment() {
    override val mRefreshLayout: KRefreshLayout
        get() = refreshLayout

    var count = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_krecyclerview, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        refreshLayout.setHeader(ClassicalHeader(context))
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
            }

            override fun getItemCount(): Int {
                return count
            }

            override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {

                return object : RecyclerView.ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_sample, parent, false)) {}
            }
        }
        recyclerView.adapter = adapter

        recyclerView.setLoadMoreView(ClassicalFooter(context))

        refreshLayout.setKRefreshListener {
            refreshLayout.postDelayed({
                count = 10
                adapter.notifyDataSetChanged()
                recyclerView.hasMore = true
                refreshLayout?.refreshComplete(true)
            }, refreshTime)
        }

        refreshLayout.startRefresh()

        recyclerView.setLoadMoreListener {
            recyclerView.postDelayed({
                count += 10
                adapter.notifyDataSetChanged()
                recyclerView.loadMoreComplete(count <= 20)
            }, 2000)
        }


    }

}