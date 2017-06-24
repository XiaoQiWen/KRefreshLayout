package gorden.krefreshlayout.demo.ui

import android.animation.ValueAnimator
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation

import gorden.krefreshlayout.demo.R
import gorden.krefreshlayout.demo.util.DensityUtil
import gorden.krefreshlayout.demo.util.XLog
import kotlinx.android.synthetic.main.activity_wechat.*

private val rotateAnimation = RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
class WechatActivity : AppCompatActivity() {
    var mOffset = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wechat)

        rotateAnimation.duration = 800
        rotateAnimation.interpolator = LinearInterpolator()
        rotateAnimation.repeatCount = Animation.INFINITE
        rotateAnimation.repeatMode = Animation.RESTART

        btn_back.setOnClickListener {
            finish()
        }
        val refreshHeight = DensityUtil.dip2px(50)
        refreshLayout.setKScrollListener { offset, distance, percent, refreshing ->
            imgChat.rotation = -distance.toFloat()
            if (!refreshing){
                if (percent<=1){
                    var tempOffset = offset
                    if (mOffset+offset!=distance){
                        tempOffset = distance-mOffset
                    }

                    mOffset+=tempOffset
                    imgChat.offsetTopAndBottom(tempOffset)
                }else if (mOffset!=refreshHeight){
                    val tempOffset = refreshHeight-mOffset
                    mOffset+=tempOffset
                    imgChat.offsetTopAndBottom(tempOffset)
                }
            }

        }


        val returnAnima:ValueAnimator = ValueAnimator()
        returnAnima.duration=500
        returnAnima.addUpdateListener {
            animation ->
            val value = animation.animatedValue as Int
            XLog.e("XXXXXXXXXXXX", "   $value  $mOffset")
            imgChat.offsetTopAndBottom(value-mOffset)
            imgChat.rotation = mOffset.toFloat()
            mOffset=value
        }

        refreshLayout.setKRefreshListener {
            imgChat.startAnimation(rotateAnimation)
            refreshLayout.postDelayed({
                refreshLayout.refreshComplete(true)
                imgChat.clearAnimation()
                returnAnima.setIntValues(mOffset,0)
                returnAnima.start()
            },3000)
        }

    }
}
