package com.nathanespejo.safeguardwildlife.Model

class Animal(var animal:String?, var date:String?, var location:String?): java.io.Serializable {
    override fun toString(): String{
        return "('$animal', '$date', '$location')"
    }
}