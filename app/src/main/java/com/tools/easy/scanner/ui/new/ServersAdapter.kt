package com.tools.easy.scanner.ui.new

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tools.easy.scanner.App
import com.tools.easy.scanner.R
import com.tools.easy.scanner.datas.entity.ServerEntity

/**
 *  description :
 */
class ServersAdapter(private val ctx: Context, private val listener: OnClickConnListener): RecyclerView.Adapter<ServersAdapter.SH>() {

    inner class SH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivFlag: ImageView = itemView.findViewById(R.id.img_sv)
        val tvServerName: TextView = itemView.findViewById(R.id.tv_sv)
    }

    interface OnClickConnListener {
        fun onClicked(connBean: ServerEntity)
    }


    private var mList = ArrayList<ServerEntity>()
    @SuppressLint("NotifyDataSetChanged")
    fun setDataResource(list: ArrayList<ServerEntity>) {
        mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SH {
        val view = LayoutInflater.from(ctx).inflate(R.layout.item_server, parent, false)
        val holder = SH(view)
        view.setOnClickListener {
            val pos = holder.layoutPosition
            if (pos < 0 || mList.isNullOrEmpty() || pos >= mList.size) return@setOnClickListener
            listener.onClicked(mList[pos])
        }
        return holder
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: SH, position: Int) {
        if (position < 0 || position >= mList.size) return
        val data = mList[position]
        if (data.isFaster) {
            holder.tvServerName.text = data.name
        } else {
            holder.tvServerName.text = "${data.code.uppercase()} - ${data.name}"
        }

        holder.ivFlag.setImageResource(App.servFlagByCode(data.code))
    }

    override fun getItemCount(): Int {
        return mList.size
    }
}