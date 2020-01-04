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
import kotlinx.android.synthetic.main.item_profile_route.view.*

class RouteRecyclerViewAdapter(val context: Context, private val items: ArrayList<Route>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.item_profile_route, parent, false)
        return RouteRecyclerViewAdapterVH(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = holder.itemView
        item.name.text = items[position].name
        item.route_distance.text = items[position].distanceText
        item.route_time.text = items[position].timeText

        if (items[position].type.equals("mountain")) {
            item.image.setImageResource(R.drawable.ic_cyclist_mountain)
        }
        if (items[position].type.equals("city")) {
            item.image.setImageResource(R.drawable.ic_cyclist_city)
        }
        if (items[position].type.equals("road")) {
            item.image.setImageResource(R.drawable.ic_cyclist_road)
        }
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

    class RouteRecyclerViewAdapterVH(v: View) : RecyclerView.ViewHolder(v) {
        var name: TextView = v.findViewById<View>(R.id.name) as TextView

    }
}
