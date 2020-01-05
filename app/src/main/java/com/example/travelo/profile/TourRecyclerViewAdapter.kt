package com.example.travelo.profile

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.travelo.R
import com.example.travelo.lib.ItemAnimation
import com.example.travelo.models.Route
import com.example.travelo.models.Tour
import com.example.travelo.models.User
import kotlinx.android.synthetic.main.item_profile_tour.view.*

class TourRecyclerViewAdapter(val context: Context, private val items: ArrayList<Triple<Tour, Route, ArrayList<User>>>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.item_profile_tour, parent, false)
        return TourRecyclerViewAdapterVH(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = holder.itemView
        item.tour_name.text = items[position].first.name

        items[position].third.forEach {
            if (items[position].first.host.equals(it.id)) {
                Glide.with(context).load(it.image).into(item.host_profile_image)
            }
        }

        item.tour_date.text = items[position].first.date
        item.tour_date_time.text = items[position].first.dateTime
        item.number_of_participants.text = "Liczba uczestników: " + items[position].third.size.toString()
        item.route_name.text = items[position].second.name
        when (items[position].second.type) {
            "mountain" -> {
                item.ic_route_type.setImageResource(R.drawable.ic_cyclist_mountain)
                item.route_type.text = "Trasa górska"
            }
            "city" -> {
                item.ic_route_type.setImageResource(R.drawable.ic_cyclist_city)
                item.route_type.text = "Trasa miejska"
            }
            "road" -> {
                item.ic_route_type.setImageResource(R.drawable.ic_cyclist_road)
                item.route_type.text = "Trasa szosowa"
            }
        }
        item.route_time.text = items[position].second.timeText
        item.route_distance.text = items[position].second.distanceText

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
