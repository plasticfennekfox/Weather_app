package com.example.dplm.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import com.example.dplm.MainViewModel
import com.example.dplm.R
import com.example.dplm.adapter.weatherAdapter
import com.example.dplm.adapter.weatherModel
import com.example.dplm.databinding.FragmentDaysBinding


class DaysFragment : Fragment(), weatherAdapter.Listener {
    private lateinit var adapter: weatherAdapter
    private lateinit var binding:FragmentDaysBinding
    private val model:MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= FragmentDaysBinding.inflate(inflater,container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        model.liveDataList.observe(viewLifecycleOwner){
            adapter.submitList(it)
        }
    }

    private fun init() = with(binding){
        adapter=weatherAdapter(this@DaysFragment)
        rcView.layoutManager=LinearLayoutManager(activity)
        rcView.adapter=adapter
    }

    companion object {

        @JvmStatic
        fun newInstance() = DaysFragment()
    }

    override fun onClick(item: weatherModel) {
        model.liveDataCurrent.value=item
    }
}