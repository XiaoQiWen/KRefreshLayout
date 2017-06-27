package gorden.krefreshlayout.demo.ui

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.FragmentManager

import gorden.krefreshlayout.demo.R
import gorden.krefreshlayout.demo.ui.fragment.*
import kotlinx.android.synthetic.main.activity_sample.*

class SampleActivity : AppCompatActivity() {
    private var mFragment:ISampleFragment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)

        text_title.text = intent.getStringExtra("name")

        btn_back.setOnClickListener {
            finish()
        }

        btn_setting.setOnClickListener {
            val intent = Intent("SettingActivity")
            intent.putExtra("header",mFragment?.headerPosition)
            intent.putExtra("pincontent",mFragment?.pinContent)
            intent.putExtra("keepheader",mFragment?.keepHeaderWhenRefresh)
            intent.putExtra("durationoffset",mFragment?.durationOffset)
            intent.putExtra("refreshtime",mFragment?.refreshTime)
            startActivityForResult(intent,612)
        }

        val position = intent.getIntExtra("position",0)

        val manager:FragmentManager = supportFragmentManager

        when(position){
            0-> mFragment = SampleAFragment()
            1-> mFragment = SampleBFragment()
            2-> mFragment = SampleCFragment()
            3-> mFragment = SampleDFragment()
            4-> mFragment = SampleEFragment()
            5-> mFragment = SampleFFragment()
            6-> mFragment = SampleGFragment()
            7-> mFragment = SampleHFragment()
            8-> mFragment = SampleIFragment()
            9-> mFragment = SampleJFragment()
            else ->mFragment = SampleAFragment()
        }
        manager.beginTransaction().replace(R.id.frame_content,mFragment).commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 612&&resultCode == Activity.RESULT_OK){
            mFragment?.headerPosition = data!!.getIntExtra("header",mFragment!!.headerPosition)
            mFragment?.pinContent = data.getBooleanExtra("pincontent",mFragment!!.pinContent)
            mFragment?.keepHeaderWhenRefresh = data.getBooleanExtra("keepheader",mFragment!!.keepHeaderWhenRefresh)
            mFragment?.durationOffset = data.getLongExtra("durationoffset",mFragment!!.durationOffset)
            mFragment?.refreshTime = data.getLongExtra("refreshtime",mFragment!!.refreshTime)
        }
    }
}
