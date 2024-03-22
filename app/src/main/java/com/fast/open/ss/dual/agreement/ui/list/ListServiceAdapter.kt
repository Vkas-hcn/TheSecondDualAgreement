package com.fast.open.ss.dual.agreement.ui.list

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fast.open.ss.dual.agreement.R
import com.fast.open.ss.dual.agreement.app.App
import com.fast.open.ss.dual.agreement.bean.VpnServiceBean
import com.fast.open.ss.dual.agreement.utils.SmileUtils.getSmileImage

class ListServiceAdapter(private val dataList: MutableList<VpnServiceBean>) :
    RecyclerView.Adapter<ListServiceAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_country)
        var aivFlag: ImageView = itemView.findViewById(R.id.aiv_flag)
        var llItem: LinearLayout = itemView.findViewById(R.id.ll_item)
        var imgSmart: ImageView = itemView.findViewById(R.id.img_smart)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(position)
                }
            }
        }
    }
    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }
    private fun onItemClick(position: Int) {
        onItemClickListener?.onItemClick(position)
    }
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    private var onItemClickListener: OnItemClickListener? = null


    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]

        if (item.best_smart) {
            holder.tvName.text = "Faster Server"
            holder.aivFlag.setImageResource(R.drawable.fast)
            holder.imgSmart.visibility = View.VISIBLE
        } else {
            holder.tvName.text = String.format(item.country_name + "," + item.city)
            holder.aivFlag.setImageResource(item.country_name.getSmileImage())
            holder.imgSmart.visibility = View.GONE
        }

        if (item.check_smart && App.vpnLink) {
            holder.llItem.background =
                holder.itemView.context.getDrawable(R.drawable.bg_item)
        } else {
            holder.llItem.background = null
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context: Context = parent.context
        val inflater = LayoutInflater.from(context)
        val itemView: View = inflater.inflate(R.layout.item_service, parent, false)
        return ViewHolder(itemView)
    }



}