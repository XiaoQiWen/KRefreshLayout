package gorden.krefreshlayout.demo.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import gorden.krefreshlayout.demo.R
import gorden.krefreshlayout.demo.header.ClassicalHeader
import gorden.krefreshlayout.demo.header.rentals.RentalsSunHeaderView
import gorden.refresh.KRefreshLayout
import kotlinx.android.synthetic.main.layout_nestedscrollview.*

/**
 * document
 * Created by Gordn on 2017/6/21.
 */
class SampleCFragment : ISampleFragment() {

    override val mRefreshLayout: KRefreshLayout
        get() = refreshLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_nestedscrollview,container,false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshLayout.setHeaderView(ClassicalHeader(context))
        refreshLayout.setKRefreshListener {
            refreshLayout.postDelayed({
                refreshLayout?.refreshComplete(true)
            },refreshTime)
        }

    }

}