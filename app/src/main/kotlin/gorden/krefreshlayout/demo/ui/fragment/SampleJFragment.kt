package gorden.krefreshlayout.demo.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import gorden.krefreshlayout.demo.R
import gorden.krefreshlayout.demo.header.ClassicalHeader_J
import kotlinx.android.synthetic.main.layout_recyclerview_j.*

/**
 * document
 * Created by Gordn on 2017/6/21.
 */
class SampleJFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_recyclerview_j, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        refreshLayout.setHeaderView(ClassicalHeader_J(context))
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        recyclerView.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
            }

            override fun getItemCount(): Int {
                return 20
            }

            override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {

                return object : RecyclerView.ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_sample, parent, false)) {}
            }
        }

        refreshLayout.setKRefreshListener {
            refreshLayout.postDelayed({
                refreshLayout?.refreshComplete(true)
            }, 3000)
        }

    }

}