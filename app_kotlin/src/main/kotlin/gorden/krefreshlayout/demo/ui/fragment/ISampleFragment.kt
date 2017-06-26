package gorden.krefreshlayout.demo.ui.fragment

import android.support.v4.app.Fragment
import android.view.ViewGroup
import gorden.krefreshlayout.demo.header.ClassicalHeader
import gorden.krefreshlayout.demo.header.WechatHeader
import gorden.krefreshlayout.demo.header.circle.CircleHeader
import gorden.krefreshlayout.demo.header.fungame.FunGameHeader
import gorden.krefreshlayout.demo.header.materia.MateriaProgressHeader
import gorden.krefreshlayout.demo.header.rentals.RentalsSunHeaderView
import gorden.krefreshlayout.demo.header.storehouse.StoreHouseHeader
import gorden.krefreshlayout.demo.util.DensityUtil
import gorden.refresh.KRefreshLayout

/**
 * document
 * Created by Gordn on 2017/6/21.
 */
abstract class ISampleFragment : Fragment() {
    abstract val mRefreshLayout: KRefreshLayout
    var headerPosition: Int = 0
        get() = field
        set(value) {
            if (field != value) {
                field = value
                header()
            }
        }

    var pinContent: Boolean = false
        get() = field
        set(value) {
            if (field != value) {
                field = value
                mRefreshLayout.pinContent = field
            }
        }

    var keepHeaderWhenRefresh: Boolean = true
        get() = field
        set(value) {
            if (field != value) {
                field = value
                mRefreshLayout.keepHeaderWhenRefresh = field
            }
        }

    var durationOffset: Long = 200
        get() = field
        set(value) {
            if (field != value) {
                field = value
                mRefreshLayout.durationOffset=field
            }
        }

    var refreshTime: Long = 2000
        get() = field
        set(value) {
            field = value
        }

    fun header(): Unit {
        when (headerPosition) {
            0 -> mRefreshLayout.setHeader(ClassicalHeader(context))
            1 -> mRefreshLayout.setHeader(MateriaProgressHeader(context), ViewGroup.LayoutParams.MATCH_PARENT, DensityUtil.dip2px(80))
            2 -> mRefreshLayout.setHeader(RentalsSunHeaderView(context))
            3 -> mRefreshLayout.setHeader(StoreHouseHeader(context))
            4 -> mRefreshLayout.setHeader(CircleHeader(context))
            5 -> mRefreshLayout.setHeader(FunGameHeader(context))
            6 -> mRefreshLayout.setHeader(WechatHeader(context))
            else -> mRefreshLayout.removeHeader()
        }
    }
}