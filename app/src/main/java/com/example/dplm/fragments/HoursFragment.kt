package com.example.dplm.fragments

import android.os.Binder
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dplm.MainViewModel
import com.example.dplm.R
import com.example.dplm.adapter.weatherAdapter
import com.example.dplm.adapter.weatherModel
import com.example.dplm.databinding.FragmentHoursBinding
import org.json.JSONArray
import org.json.JSONObject


class HoursFragment : Fragment() {
    private lateinit var binding: FragmentHoursBinding
    private lateinit var adapter:weatherAdapter
    private val model:MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding=FragmentHoursBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRcView()
        model.liveDataCurrent.observe(viewLifecycleOwner){
            Log.d("Mylog", "hours:${it.hours}")
            adapter.submitList(getHoursList(it))
        }

    }

    private fun initRcView()= with(binding){
        rcView.layoutManager=LinearLayoutManager(activity)
        adapter=weatherAdapter(null)
        rcView.adapter=adapter

    }

    private fun getHoursList(wItem:weatherModel):List<weatherModel>{
        val hoursArray = JSONArray(wItem.hours)
        val list = ArrayList<weatherModel>()
        for (i in 0 until hoursArray.length()){
            val item = weatherModel(
                wItem.city,
                (hoursArray[i] as JSONObject).getString("time"),
                (hoursArray[i] as JSONObject).getJSONObject("condition").getString("text"),
                (hoursArray[i] as JSONObject).getString("temp_c")+"°C",
                "",
                "",
                (hoursArray[i] as JSONObject).getJSONObject("condition").getString("icon"),
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