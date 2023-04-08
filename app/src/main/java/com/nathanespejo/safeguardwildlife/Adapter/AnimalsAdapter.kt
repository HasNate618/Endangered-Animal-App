package com.nathanespejo.safeguardwildlife.Adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.nathanespejo.safeguardwildlife.Model.Animals

class AnimalsAdapter(internal  var context: Context, internal var animalsList:List<Animals>):RecyclerView.Adapter<AnimalsAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View):RecyclerView.ViewHolder(itemView),View.OnClickListener {
        //internal var root_view:CardView

        override fun onClick(p0: View?) {
            TODO("Not yet implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        return animalsList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        TODO("Not yet implemented")
    }
}