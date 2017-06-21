package gorden.krefreshlayout.demo.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.SimpleAdapter
import android.widget.Toast
import gorden.krefreshlayout.demo.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    val gridItems = arrayOf("RecyclerView","ScrollView","NestedScrollView","ViewPager in NestedScrollView","RecyclerView"
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

        gridView.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent("SampleActivity")
            intent.putExtra("position",position);
            intent.putExtra("name",gridItems[position])
            startActivity(intent)
        }

        refreshLayout.setKRefreshListener {
            refreshLayout.postDelayed({
            refreshLayout.refreshComplete(true)
            },2000)
        }

//        refreshLayout.startRefresh()
    }
}
