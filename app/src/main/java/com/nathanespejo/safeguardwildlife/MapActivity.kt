package com.nathanespejo.safeguardwildlife

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.mancj.materialsearchbar.MaterialSearchBar
import com.mancj.materialsearchbar.MaterialSearchBar.BUTTON_BACK
import com.nathanespejo.safeguardwildlife.API.DatabaseAPI
import com.nathanespejo.safeguardwildlife.Model.Animal

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var mMap: GoogleMap
    lateinit var search_bar: MaterialSearchBar
    lateinit var submission: Animal
    val mapZoom = 4f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        submission = intent.getSerializableExtra("Submission") as Animal
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        search_bar = findViewById(R.id.searchBar)
        search_bar.setCardViewElevation(10)
        search_bar.setOnSearchActionListener(object:MaterialSearchBar.OnSearchActionListener {
            override fun onSearchStateChanged(enabled: Boolean) {
                if (!enabled){
                    val objGet = DatabaseAPI.Get()
                    objGet.start(::onAnimalsReceived)
                }
            }
            override fun onSearchConfirmed(text: CharSequence?) {
                val objQuery = DatabaseAPI.Query()
                objQuery.start(::onAnimalsReceived, text.toString())
            }
            override fun onButtonClicked(buttonCode: Int) {
            }
        })
    }

    private fun onAnimalsReceived(animalsList: List<Animal>){
        Log.d("LOGS", animalsList.count().toString() + " animals found")
        if (this::mMap.isInitialized){
            mMap.clear()
            Toast.makeText(this@MapActivity, animalsList.count().toString() + " entries found", Toast.LENGTH_SHORT).show()
            var lastLocation = LatLng(0.00,0.00)

            for (animal in animalsList) {
                val latLong = animal.location?.split(",")?.toTypedArray()
                val location = LatLng(latLong?.get(0)?.toDouble()!!, latLong?.get(1)?.toDouble()!!)
                lastLocation = location
                val animalName = animal.animal
                val date = animal.date

                mMap.addMarker(MarkerOptions().position(location).title(animalName + " " + date))
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, mapZoom))
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        mMap = p0

        if (submission.animal != "null"){
            val latLong = submission.location?.split(",")?.toTypedArray()
            val location = LatLng(latLong?.get(0)?.toDouble()!!, latLong?.get(1)?.toDouble()!!)
            val animalName = submission.animal
            val date = submission.date

            mMap.addMarker(MarkerOptions().position(location).title(animalName + " " + date))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, mapZoom))
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this, SubmissionActivity::class.java).also {
            startActivity(it)
        }
    }
}