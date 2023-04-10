package com.nathanespejo.safeguardwildlife.API

import android.os.AsyncTask
import android.util.Log
import com.nathanespejo.safeguardwildlife.Model.Animal
import java.sql.DriverManager

class DatabaseAPI {

    companion object {
        private const val DB_URL = SensitiveData.DB_URL
        private const val USER = SensitiveData.USER
        private const val PASS = SensitiveData.PASS
    }

    class Send() : AsyncTask<String?, String?, String>() {
        var msg = ""
        var animal: Animal? = null

        fun start(animal: Animal?) {
            this.animal = animal
            execute()
        }

        override fun doInBackground(vararg p0: String?): String {
            try {
                Class.forName("com.mysql.jdbc.Driver")
                val conn = DriverManager.getConnection(DB_URL, USER, PASS)
                msg = if (conn == null) {
                    "Connection went wrong"
                } else {
                    val query =
                        "INSERT INTO `animals` (`animal`, `date`, `location`) VALUES " + animal.toString() + ";"
                    val stmt = conn.createStatement()
                    stmt.executeUpdate(query)
                    "Inserted data: " + animal.toString()
                }
                conn.close()
            } catch (e: Exception) {
                msg = "Connection went wrong"
                e.printStackTrace()
            }
            return msg
            Log.d("LOGS", msg)
        }
    }

    class Get() : AsyncTask<String?, String?, String>() {
        var msg = ""
        var animals: MutableList<Animal> = mutableListOf()
        lateinit var callback: (animals:List<Animal>) -> Unit

        fun start(callbackFunction: (animals:List<Animal>) -> (Unit)) {
            callback = callbackFunction
            execute()
        }

        override fun doInBackground(vararg p0: String?): String {
            try {
                Class.forName("com.mysql.jdbc.Driver")
                val conn = DriverManager.getConnection(DB_URL, USER, PASS)
                msg = if (conn == null) {
                    "Connection went wrong"
                } else {
                    val query =
                        "SELECT * FROM animals"
                    val stmt = conn.createStatement()
                    val rs = stmt.executeQuery(query)

                    while (rs.next()){
                        val animal = rs.getString("animal")
                        val date = rs.getString("date")
                        val location = rs.getString("location")
                        val newAnimal = Animal(animal, date, location)
                        animals.add(newAnimal)
                    }

                    "Returned " + animals.count().toString() + " animals"
                }
                conn.close()
            } catch (e: Exception) {
                msg = "Connection went wrong"
                e.printStackTrace()
            }
            Log.d("LOGS", msg)
            return msg
        }

        override fun onPostExecute(result: String?) {
            callback(animals)
        }
    }

    class Query() : AsyncTask<String?, String?, String>() {
        var msg = ""
        var animals: MutableList<Animal> = mutableListOf()
        lateinit var callback: (animals:List<Animal>) -> Unit
        var searchQuery = ""

        fun start(callbackFunction: (animals:List<Animal>) -> (Unit), query:String) {
            callback = callbackFunction
            searchQuery = query
            execute()
        }

        override fun doInBackground(vararg p0: String?): String {
            try {
                Class.forName("com.mysql.jdbc.Driver")
                val conn = DriverManager.getConnection(DB_URL, USER, PASS)
                msg = if (conn == null) {
                    "Connection went wrong"
                } else {
                    val query =
                        "SELECT * FROM animals WHERE animal LIKE '%" + searchQuery + "%'"
                    val stmt = conn.createStatement()
                    val rs = stmt.executeQuery(query)

                    while (rs.next()){
                        val animal = rs.getString("animal")
                        val date = rs.getString("date")
                        val location = rs.getString("location")
                        val newAnimal = Animal(animal, date, location)
                        animals.add(newAnimal)
                    }

                    "Returned " + animals.count().toString() + " animals"
                }
                conn.close()
            } catch (e: Exception) {
                msg = "Connection went wrong"
                e.printStackTrace()
            }
            Log.d("LOGS", msg)
            return msg
        }

        override fun onPostExecute(result: String?) {
            callback(animals)
        }
    }
}