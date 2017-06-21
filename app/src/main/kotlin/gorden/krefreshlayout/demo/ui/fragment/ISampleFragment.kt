package gorden.krefreshlayout.demo.ui.fragment

import android.support.v4.app.Fragment
import android.view.ViewGroup
import gorden.krefreshlayout.demo.header.ClassicalHeader
import gorden.krefreshlayout.demo.header.NullHeader
import gorden.krefreshlayout.demo.header.materia.MateriaProgressHeader
import gorden.krefreshlayout.demo.header.rentals.RentalsSunHeaderView
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
                mRefreshLayout.setPinContent(field)
            }
        }

    var keepHeaderWhenRefresh: Boolean = true
        get() = field
        set(value) {
            if (field != value) {
                field = value
                mRefreshLayout.setKeepHeaderWhenRefresh(field)
            }
        }

    var durationOffset: Long = 200
        get() = field
        set(value) {
            if (field != value) {
                field = value
                mRefreshLayout.setDurationOffset(field)
            }
        }

    var refreshTime: Long = 2000
        get() = field
        set(value) {
            field = value
        }

    fun header(): Unit {
        when (headerPosition) {
            0 -> mRefreshLayout.setHeaderView(ClassicalHeader(context))
            1 -> mRefreshLayout.setHeaderView(MateriaProgressHeader(context), ViewGroup.LayoutParams.MATCH_PARENT, DensityUtil.dip2px(80))
            2 -> mRefreshLayout.setHeaderView(RentalsSunHeaderView(context))
            else -> mRefreshLayout.setHeaderView(NullHeader(context))
        }
    }
}