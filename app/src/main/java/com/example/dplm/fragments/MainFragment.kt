package com.example.dplm.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.media.audiofx.Equalizer.Settings
import android.os.Bundle
import android.util.JsonReader
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.dplm.MainViewModel
import com.example.dplm.R
import com.example.dplm.adapter.VpAdapter
import com.example.dplm.adapter.weatherModel
import com.example.dplm.databinding.FragmentMainBinding
import com.example.dplm.dialogManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.util.Objects
import java.util.PriorityQueue

const val API_Key = "2432de0fc8b44b23b96180736232803"


class MainFragment : Fragment() {

    private lateinit var fLocationCliet: FusedLocationProviderClient
    private val tlist = listOf(
        "HOURS",
        "DAYS"
    )

    private val flist = listOf(
        HoursFragment.newInstance(),
        DaysFragment.newInstance()
    )
    private lateinit var pLauncher: ActivityResultLauncher<String>
    private lateinit var binding: FragmentMainBinding
    private val model : MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermission()
        init()
        updateCurrentCard()
        requestWeatherData("Moscow")
        getLocation()
    }

    override fun onResume() {
        super.onResume()
        checklocation()
    }

    private fun init() = with(binding){
        fLocationCliet=LocationServices.getFusedLocationProviderClient(requireContext())
        val adapter = VpAdapter(activity as FragmentActivity, flist)
        vp.adapter = adapter
        TabLayoutMediator(tabLayout, vp){
                tab, pos -> tab.text = tlist[pos]
        }.attach()
        ibSync.setOnClickListener{
            tabLayout.selectTab(tabLayout.getTabAt(0))
            checklocation()
        }
        ibSearch.setOnClickListener{
            dialogManager.searchByName(requireContext(),object :dialogManager.Listener{
                override fun onClick(name: String?) {
                    //Log.d("Mylog","Name:$name")
                    if (name != null) {
                        requestWeatherData(name)
                    }
                }
            })
        }
    }

    private fun checklocation(){
        if(isLocationEnabled()){
            getLocation()
        }else{
            dialogManager.locationSettingsDialog(requireContext(),object :dialogManager.Listener{
                override fun onClick(name:String?) {
                    startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            })
        }
    }
    private fun isLocationEnabled():Boolean{
        val lm =activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun getLocation(){
        val ct=CancellationTokenSource()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
          }
        fLocationCliet.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, ct.token)
            .addOnCompleteListener{
                requestWeatherData("${it.result.latitude},${it.result.longitude}")
            }
    }

    private fun updateCurrentCard()= with(binding){
        model.liveDataCurrent.observe(viewLifecycleOwner){
            val maxmintemp="${it.maxTemp}°C/${it.minTemp}°C"
            tvData.text=it.time
            tvCity.text=it.city
            tvCurrentTemp.text=it.currentTemp.ifEmpty { maxmintemp }
            tvCondition.text=it.condition
            tvMaxMin.text=if(it.currentTemp.isEmpty())"" else maxmintemp
            Picasso.get().load("https:"+it.imageURL).into(imWeather)

        }
    }

    private fun permissionListener(){
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()){
            Toast.makeText(activity, "Permission is $it", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkPermission(){
        if(!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)){
            permissionListener()
            pLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun requestWeatherData(city: String) {
        val url = "http://api.weatherapi.com/v1/forecast.json?key=" +
                API_Key +
                "&q=" +
                city +
                "&days=" +
                "3" +
                "&aqi=no&alerts=no"
        val queue = Volley.newRequestQueue(context)
        val request = StringRequest(
            Request.Method.GET,
            url,
            {
                    result -> parseWeatherData(result)
            },
            {
                    error -> Log.d("MyLog", "Error: $error")
            }
        )
        queue.add(request)
    }

    private fun parseWeatherData(result: String) {
        val mainObject = JSONObject(result)
        val list = parseDays(mainObject)
        parseCurrentData(mainObject, list[0])
    }

    private fun parseDays(mainObject: JSONObject): List<weatherModel>{
        val list = ArrayList<weatherModel>()
        val daysArray = mainObject.getJSONObject("forecast")
            .getJSONArray("forecastday")
        val name =  mainObject.getJSONObject("location").getString("name")
        for (i in 0 until daysArray.length()){
            val day = daysArray[i] as JSONObject
            val item = weatherModel(
                name,
                day.getString("date"),
                day.getJSONObject("day").getJSONObject("condition")
                    .getString("text"),
                "",
                day.getJSONObject("day").getString("maxtemp_c").toFloat().toInt().toString(),
                day.getJSONObject("day").getString("mintemp_c").toFloat().toInt().toString(),
                day.getJSONObject("day").getJSONObject("condition")
                    .getString("icon"),
                day.getJSONArray("hour").toString()
            )
            list.add(item)
        }
        model.liveDataList.value=list
        return list
    }

    private fun parseCurrentData(mainObject:JSONObject,weatherItem:weatherModel){
        val item = weatherModel(
            mainObject.getJSONObject("location").getString("name"),
            mainObject.getJSONObject("current").getString("last_updated"),
            mainObject.getJSONObject("current").getJSONObject("condition")
                .getString("text"),
            mainObject.getJSONObject("current").getString("temp_c"),
            weatherItem.maxTemp,
            weatherItem.minTemp,
            mainObject.getJSONObject("current").getJSONObject("condition")
                .getString("icon"),
            weatherItem.hours
        )
        model.liveDataCurrent.value=item
        //Log.d("Mylog", "hours:${item.hours}")
        //Log.d("Mylog", "hours:${item.maxTemp}")
        //Log.d("Mylog", "hours:${item.minTemp}")
        //Log.d("Mylog", "hours:${item.condition}")
        //Log.d("Mylog", "hours:${item.currentTemp}")
        //Log.d("Mylog", "hours:${item.imageURL}")
        //Log.d("Mylog", "hours:${item.time}")
        //Log.d("Mylog", "hours:${item.city}")
    }

    companion object {

        @JvmStatic
        fun newInstance() = MainFragment()
    }
}