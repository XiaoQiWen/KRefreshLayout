package gorden.krefreshlayout.demo.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import gorden.krefreshlayout.demo.R
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {
    private var headerPosition = 0

    private var pinContent = false
    private var keepHeaderWhenRefresh = true
    private var durationOffset = 200L
    private var refreshTime = 2000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        btn_back.setOnClickListener {
            finish()
        }

        headerPosition = intent.getIntExtra("header", 0)
        pinContent = intent.getBooleanExtra("pincontent", false)
        keepHeaderWhenRefresh = intent.getBooleanExtra("keepheader", true)
        durationOffset = intent.getLongExtra("durationoffset", 200)
        refreshTime = intent.getLongExtra("refreshtime", 2000)

        spinner.setSelection(headerPosition)
        togglePinContent.isChecked = pinContent
        toggleKeepHeader.isChecked = keepHeaderWhenRefresh
        edit_offset.setText(durationOffset.toString())
        edit_refresh.setText(refreshTime.toString())

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                headerPosition = position
            }
        }
    }

    override fun finish() {
        val data = Intent()

        pinContent = togglePinContent.isChecked
        keepHeaderWhenRefresh = toggleKeepHeader.isChecked
        durationOffset = if (TextUtils.isEmpty(edit_offset.text)) 200 else edit_offset.text.toString().toLong()
        refreshTime = if (TextUtils.isEmpty(edit_refresh.text)) 2000 else edit_refresh.text.toString().toLong()

        data.putExtra("header", headerPosition)
        data.putExtra("pincontent", pinContent)
        data.putExtra("keepheader", keepHeaderWhenRefresh)
        data.putExtra("durationoffset", durationOffset)
        data.putExtra("refreshtime", refreshTime)
        setResult(Activity.RESULT_OK, data)
        super.finish()
    }
}
