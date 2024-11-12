package com.gv.wetherapp1.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class VpAdapter(fa:FragmentActivity,private val list:List<Fragment>):FragmentStateAdapter(fa) {
    //опеределяем, сколько у нас позиций в TabLauout
    override fun getItemCount(): Int {
       return list.size
    }
// переключение по позициям
    override fun createFragment(position: Int): Fragment {
        return list[position]
    }
}