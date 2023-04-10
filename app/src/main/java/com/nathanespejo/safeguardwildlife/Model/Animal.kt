package com.nathanespejo.safeguardwildlife.Model

class Animal(var animal:String?, var date:String?, var location:String?) {
    override fun toString(): String{
        return "('$animal', '$date', '$location')"
    }
}