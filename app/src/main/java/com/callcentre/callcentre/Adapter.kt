package com.callcentre.callcentre

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater

import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * Created by Nikolay on 12.12.2017.
 */
class Adapter : RecyclerView.Adapter<Adapter.ViewHolder>() {

    private val data: MutableList<String> = mutableListOf()

    lateinit var recyclerView:RecyclerView

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val text = data[position]

        holder?.let {
            it.text.text = text
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.data_item, parent, false)
        return ViewHolder(view)
    }

    fun add(mesage:String){
        this.data.add(mesage)
        recyclerView.layoutManager.scrollToPosition(data.size-1)
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text:TextView = view.findViewById(R.id.text)
    }

}