package com.tools.easy.scanner.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.tools.easy.scanner.R
import com.tools.easy.scanner.ui.home.MainActivity

/**
 *  description :
 */
class CardAdapter(private val context: Context, private val list: List<MainActivity.TypeItem>) : RecyclerView.Adapter<CardAdapter.ViewHolder>() {

    private var onClick: ((item: MainActivity.TypeItem) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.icon.setImageResource(item.icon)
        holder.name.setImageResource(item.nameImg)
        holder.itemView.setOnClickListener {
            onClick?.invoke(item)
        }
    }

    override fun getItemCount(): Int = list.size

    fun setOnClick(onClick: (item: MainActivity.TypeItem) -> Unit) {
        this.onClick = onClick
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.img_ic)
        val name: ImageView = itemView.findViewById(R.id.img_name)
    }
}