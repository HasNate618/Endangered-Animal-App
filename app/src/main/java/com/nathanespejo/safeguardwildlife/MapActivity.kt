package com.nathanespejo.safeguardwildlife

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.SearchView
import com.nathanespejo.safeguardwildlife.API.ISearchAPI
import com.nathanespejo.safeguardwildlife.API.RetrofitClient
import com.nathanespejo.safeguardwildlife.Adapter.AnimalsAdapter

class MapActivity : AppCompatActivity() {

    internal lateinit var myAPI:ISearchAPI
    internal lateinit var adapter:AnimalsAdapter

    override fun onStop() {
        super.onStop()
    }

    private val api:ISearchAPI
    get() = RetrofitClient.getInstance().create(ISearchAPI::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        //Init API
        myAPI = api;

        //View
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu,menu)

        val search = menu?.findItem(R.id.nav_search)
        val searchView = search?.actionView as SearchView

        searchView.setOnQueryTextListener(object:SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                startSearch(p0.toString())
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                getAllAnimals()
                return true
            }

        })

        return super.onCreateOptionsMenu(menu)
    }

    private fun startSearch(query:String) {
        for(i in 0..myAPI.animalList.size){
            val a = myAPI.animalList[i]
            val infoText = a.animal + "," + a.date + "," + a.location
            Log.d("ANIMALS", infoText)
        }
    }

    private fun getAllAnimals() {
        TODO("Not yet implemented")
    }
}