package com.example.dplm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dplm.R
import com.example.dplm.databinding.ListItemBinding
import com.squareup.picasso.Picasso

class weatherAdapter (val listener: Listener?) : ListAdapter <weatherModel,weatherAdapter.Holder>(Comporator()) {

    class Holder(view:View, val listener: Listener?):RecyclerView.ViewHolder(view){
        val binding = ListItemBinding.bind(view)
        var itemtemp:weatherModel? = null
        init{
            itemView.setOnClickListener{
                itemtemp?.let { it1 -> listener?.onClick(it1) }
            }
        }

        fun bind(item: weatherModel) = with(binding){
            itemtemp=item
            tvDate.text = item.time
            tvCondition.text=item.condition
            tvTemp.text=item.currentTemp.ifEmpty { "${item.maxTemp}°C / ${item.minTemp}°C" }
            Picasso.get().load("https:"+item.imageURL).into(im)

        }
    }
    class Comporator:DiffUtil.ItemCallback<weatherModel>(){
        override fun areItemsTheSame(oldItem: weatherModel, newItem: weatherModel): Boolean {
            return oldItem==newItem
        }
        override fun areContentsTheSame(oldItem: weatherModel, newItem: weatherModel): Boolean {
            return oldItem==newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val  view = LayoutInflater.from(parent.context).inflate(R.layout.list_item,parent,false)
        return Holder(view,listener)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }
    interface Listener{
        fun onClick(item: weatherModel)
    }
}