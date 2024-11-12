package com.gv.wetherapp1.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gv.wetherapp1.R
import com.gv.wetherapp1.databinding.ListItemBinding
import com.squareup.picasso.Picasso

class WeatherAdapter(val listener: Listener?) :ListAdapter<WeatherModel,WeatherAdapter.Holder>(Comporator()){
    //Отвечает за какой-то конкретный элемент например текст вью
    class Holder(view: View,val listener: Listener?):RecyclerView.ViewHolder(view){
        //берем из сервера список, и с помощью этой функции заполняем текст вью
        val binding = ListItemBinding.bind(view)
var itemTemp:WeatherModel?=null
        init{
            itemView.setOnClickListener {
                itemTemp?.let { it1 -> listener?.onClick(it1) }
            }
        }

        fun bind(item:WeatherModel)=with (binding ){
            itemTemp=item
            tvDate.text=item.time
            tvCondition.text=item.condition
            tvTemp.text=item.currentTemp.ifEmpty { "${item.minTemp}°C/${item.maxTemp}°C" }
            Picasso.get().load("https:"+ item.imageUrl).into(im)

        }

    }
    class Comporator: DiffUtil.ItemCallback<WeatherModel>() {
        //при каждом пролистовании эти два метода ходят по элементам списка и сравниваю их со старым списком, если они не совпадают переписвает эелемент
        override fun areItemsTheSame(oldItem: WeatherModel, newItem: WeatherModel): Boolean {
            return oldItem==newItem
        }

        override fun areContentsTheSame(oldItem: WeatherModel, newItem: WeatherModel): Boolean {
            return oldItem==newItem
        }
    }
    //С помощью этого метода наши вью загружаются в память
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent,false)
    return Holder(view,listener)
    }
    //Тут мы начинаем заполнять наши сохраненные вью
    override fun onBindViewHolder(holder: Holder, position: Int) {
       holder.bind(getItem(position))
    }

    interface Listener{
        fun onClick(item:WeatherModel)
    }
}