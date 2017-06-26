package gorden.krefreshlayout.demo.ui.fragment

import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import gorden.krefreshlayout.demo.R
import gorden.krefreshlayout.demo.header.ClassicalHeader
import gorden.refresh.KRefreshLayout
import kotlinx.android.synthetic.main.layout_coordinatorlayout.*

/**
 * document
 * Created by Gordn on 2017/6/21.
 */
class SampleHFragment : ISampleFragment() {
    override val mRefreshLayout: KRefreshLayout
        get() = refreshLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_coordinatorlayout, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        refreshLayout.setHeader(ClassicalHeader(context))
        recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        textView.addOnLayoutChangeListener {
            _, _, _, _, _, _, _, _, _ ->
            refreshLayout.refreshEnable = scrolling_header.translationY==0f
        }

        recyclerView.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
            }

            override fun getItemCount(): Int {
                return 20
            }

            override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {

                return object : RecyclerView.ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_sample,parent,false)){}
            }
        }

        refreshLayout.setKRefreshListener {
            refreshLayout.postDelayed({
                refreshLayout?.refreshComplete(true)
            },refreshTime)
        }
    }

}