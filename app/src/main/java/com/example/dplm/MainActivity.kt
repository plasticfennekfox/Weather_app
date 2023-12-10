package com.example.dplm

import android.app.DownloadManager.Request
import android.os.Binder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.dplm.databinding.ActivityMainBinding
import com.example.dplm.fragments.MainFragment
import org.json.JSONObject

//const val API_key = "2432de0fc8b44b23b96180736232803"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportFragmentManager.beginTransaction().replace(R.id.placeHolder,MainFragment.newInstance()).commit()

    }
}
