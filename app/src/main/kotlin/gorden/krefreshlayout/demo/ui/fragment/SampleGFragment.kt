package gorden.krefreshlayout.demo.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import gorden.krefreshlayout.demo.R
import gorden.krefreshlayout.demo.header.ClassicalHeader
import gorden.refresh.KRefreshLayout
import kotlinx.android.synthetic.main.layout_viewpager.*

/**
 * document
 * Created by Gordn on 2017/6/21.
 */
class SampleGFragment : ISampleFragment() {
    var currentPosition = 0
    val fragmentList = arrayListOf(SampleAFragment(), SampleDFragment(), SampleCFragment())
    val titleList = arrayListOf("TAB1","TAB2","TAB3")

    override val mRefreshLayout: KRefreshLayout
        get() = fragmentList[currentPosition].mRefreshLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_viewpager, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabLayout.setupWithViewPager(viewPager)

        viewPager.adapter = object : FragmentStatePagerAdapter(fragmentManager) {
            override fun getItem(position: Int): Fragment {
                return fragmentList[position]
            }

            override fun getCount(): Int {
                return 3
            }

            override fun getPageTitle(position: Int): CharSequence {
                return titleList[position]
            }

        }

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                currentPosition = position
            }

            override fun onPageScrollStateChanged(state: Int) {
            }

        })
    }

}