package com.example.travelo.profile

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.travelo.R
import com.example.travelo.lib.ItemAnimation
import com.example.travelo.models.Route
import com.example.travelo.models.Tour
import kotlinx.android.synthetic.main.item_profile_route.view.*

class TourRecyclerViewAdapter(val context: Context, private val items: ArrayList<Tour>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.item_profile_tour, parent, false)
        return TourRecyclerViewAdapterVH(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = holder.itemView
        setAnimation(holder.itemView, position)
    }

    private var lastPosition = -1
    private val on_attach = true

    private fun setAnimation(view: View, position: Int) {
        if (position > lastPosition) {
            ItemAnimation.animate(view, position, ItemAnimation.FADE_IN)
            lastPosition = position
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class TourRecyclerViewAdapterVH(v: View) : RecyclerView.ViewHolder(v) {
        var name: TextView = v.findViewById<View>(R.id.tour_name) as TextView

    }
}
