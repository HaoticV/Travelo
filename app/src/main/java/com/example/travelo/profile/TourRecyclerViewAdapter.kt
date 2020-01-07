package com.example.travelo.profile

import android.content.Context
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


    private val TOUR_VIEW = 1
    private val SECTION_VIEW = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TOUR_VIEW) {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_profile_tour, parent, false)
            return TourRecyclerViewAdapterVH(v)
        } else {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_section, parent, false)
            return SectionViewHolder(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = holder.itemView
        if (items[position].first is Tour) {
            val tour = (items[position].first) as Tour
            val route = (items[position].second) as Route
            @Suppress("UNCHECKED_CAST") val user = (items[position].third) as ArrayList<User>

            item.tour_name.text = tour.name

            user.forEach {
                if (tour.host == it.id) {
                    Glide.with(context).load(it.image).into(item.host_profile_image)
                }
            }

            item.tour_date.text = tour.date
            item.tour_date_time.text = tour.dateTime
            item.number_of_participants.text = "Liczba uczestników: " + user.size.toString()
            item.route_name.text = route.name

            when (route.type) {
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
            item.route_time.text = route.timeText
            item.route_distance.text = route.distanceText
        } else {
            val view: SectionViewHolder = holder as SectionViewHolder
            view.sectionTitle.text = items[position].toString()
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

    override fun getItemViewType(position: Int): Int {
        if (items[position].first is Tour)
            return TOUR_VIEW
        else
            return SECTION_VIEW
    }

    class TourRecyclerViewAdapterVH(v: View) : RecyclerView.ViewHolder(v) {
        var name: TextView = v.findViewById<View>(R.id.tour_name) as TextView

    }

    class SectionViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var sectionTitle: TextView = v.findViewById(R.id.title_section)
    }
}
