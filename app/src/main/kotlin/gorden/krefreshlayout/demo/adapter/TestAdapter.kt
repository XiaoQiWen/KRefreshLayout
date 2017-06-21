package gorden.kotlin

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.Adapter
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import gorden.krefreshlayout.demo.R
import kotlinx.android.synthetic.main.item_test.view.*

/**
 * document
 * Created by Gordn on 2017/5/26.
 */
class TestAdapter(var data: Array<String>) : Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return object : RecyclerView.ViewHolder(View.inflate(parent.context, R.layout.item_test, null)){}
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Glide.with(holder.itemView.context).load(data[position]).asBitmap().centerCrop().into(holder.itemView.textTest)
    }

    override fun getItemCount(): Int {
        return data.size
    }

}