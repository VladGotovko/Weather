package com.gv.wetherapp1.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.gv.wetherapp1.MainViewModel
import com.gv.wetherapp1.R
import com.gv.wetherapp1.adapters.WeatherAdapter
import com.gv.wetherapp1.adapters.WeatherModel
import com.gv.wetherapp1.databinding.FragmentHoursBinding
import org.json.JSONArray
import org.json.JSONObject


class HoursFragment : Fragment() {
private lateinit var binding: FragmentHoursBinding
private lateinit var adapter: WeatherAdapter
private val model:MainViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
binding=FragmentHoursBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRcView()
        model.liveDataCurrent.observe(viewLifecycleOwner){
        adapter.submitList(getHoursList(it))
        }
    }
    private fun initRcView()=with(binding){
        rcView.layoutManager=LinearLayoutManager(activity)
        adapter= WeatherAdapter(null)
        rcView.adapter=adapter

    }
    private fun getHoursList(wItem:WeatherModel):List<WeatherModel>{
        val hoursArray = JSONArray(wItem.hours)
        val list=ArrayList<WeatherModel>()
        for (i in 0 until hoursArray.length()) {
val item=WeatherModel(
    wItem.city,
    (hoursArray[i] as JSONObject).getString("time"),
    (hoursArray[i] as JSONObject).getJSONObject("condition")
        .getString("text"),
    (hoursArray[i] as JSONObject).getString("temp_c").toFloat().toInt().toString(),
    "",
    "",
    (hoursArray[i] as JSONObject).getJSONObject("condition")
        .getString("icon"),
    ""
)
            list.add(item)
        }
        return list
        }

    companion object {

        @JvmStatic
        fun newInstance() = HoursFragment()

    }
}