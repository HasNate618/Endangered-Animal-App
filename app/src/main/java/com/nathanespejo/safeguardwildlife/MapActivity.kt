package com.nathanespejo.safeguardwildlife

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.mancj.materialsearchbar.MaterialSearchBar
import com.nathanespejo.safeguardwildlife.API.DatabaseAPI
import com.nathanespejo.safeguardwildlife.Model.Animal

class MapActivity : AppCompatActivity() {

    lateinit var search_bar: MaterialSearchBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        search_bar = findViewById(R.id.search_bar)
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
        for (animal in animalsList) {
            Log.d("LOGS", animalsList.indexOf(animal).toString() + ": " + animal.toString())
        }
    }
}