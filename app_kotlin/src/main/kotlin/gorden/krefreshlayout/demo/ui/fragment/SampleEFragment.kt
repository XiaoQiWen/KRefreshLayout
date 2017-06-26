package gorden.krefreshlayout.demo.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import gorden.krefreshlayout.demo.R
import gorden.krefreshlayout.demo.header.ClassicalHeader
import gorden.refresh.KRefreshLayout
import kotlinx.android.synthetic.main.layout_vp_scrollview.*

/**
 * document
 * Created by Gordn on 2017/6/21.
 */
class SampleEFragment : ISampleFragment() {

    override val mRefreshLayout: KRefreshLayout
        get() = refreshLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_vp_scrollview, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshLayout.setHeader(ClassicalHeader(context))
        viewPager.setImages(arrayListOf<Int>(R.drawable.img_pager1, R.drawable.img_pager2, R.drawable.img_pager3))

        refreshLayout.setKRefreshListener {
            refreshLayout.postDelayed({
                refreshLayout?.refreshComplete(true)
            }, refreshTime)
        }

    }

}