package com.nathanespejo.safeguardwildlife.API

import com.nathanespejo.safeguardwildlife.Model.Animals
import retrofit2.http.*

interface ISearchAPI {
    @get:GET("animals")
    val animalList: List<Animals>

    @POST("search")
    @FormUrlEncoded
    fun searchAnimals(@Field("search") searchQuery:String):List<Animals>
}