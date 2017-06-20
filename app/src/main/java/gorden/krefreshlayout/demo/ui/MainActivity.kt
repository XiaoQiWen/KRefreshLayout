package gorden.krefreshlayout.demo.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.SimpleAdapter
import gorden.krefreshlayout.demo.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    val gridItems = arrayOf("RecyclerView","ScrollView","NestedScrollView","ViewPager\nin\nNestedScrollView","RecyclerView"
            ,"RecyclerView","RecyclerView","RecyclerView","RecyclerView","RecyclerView","RecyclerView","RecyclerView"
            ,"RecyclerView","RecyclerView","RecyclerView","RecyclerView","RecyclerView","RecyclerView","RecyclerView"
            ,"RecyclerView","RecyclerView","RecyclerView","RecyclerView","RecyclerView","RecyclerView","RecyclerView")
    val dataList = ArrayList<Map<String,String>>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        for (item in gridItems){
            val map = HashMap<String,String>()
            map.put("name",item)
            dataList.add(map)
        }
        gridView.adapter = SimpleAdapter(this,dataList,R.layout.item_main, arrayOf("name"), intArrayOf(R.id.textView))

        refreshLayout.setKRefreshListener {
            refreshLayout.postDelayed({
            refreshLayout.refreshComplete(true)
            },2000)
        }

        refreshLayout.startRefresh()
    }
}
