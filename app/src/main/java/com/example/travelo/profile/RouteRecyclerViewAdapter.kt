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

class RouteRecyclerViewAdapter(val context: Context, private val items: ArrayList<Any>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == ROUTE_VIEW) {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_profile_route, parent, false)
            return RouteRecyclerViewAdapterVH(v)
        } else {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_section, parent, false)
            return SectionViewHolder(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = holder.itemView
        if (items[position] is Route) {
            var route = items[position] as Route
            item.name.text = route.name
            item.route_distance.text = route.distanceText
            item.route_time.text = route.timeText

            if (route.type.equals("mountain")) {
                item.image.setImageResource(R.drawable.ic_cyclist_mountain)
            }
            if (route.type.equals("city")) {
                item.image.setImageResource(R.drawable.ic_cyclist_city)
            }
            if (route.type.equals("road")) {
                item.image.setImageResource(R.drawable.ic_cyclist_road)
            }
        } else {
            val view: SectionViewHolder = holder as SectionViewHolder
            view.title_section.text = items[position].toString()
        }
        setAnimation(holder.itemView, position)
    }

    private val ROUTE_VIEW = 1
    private val SECTION_VIEW = 0
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
        if (items[position] is Route)
            return ROUTE_VIEW
        else
            return SECTION_VIEW
    }

    class RouteRecyclerViewAdapterVH(v: View) : RecyclerView.ViewHolder(v) {
        var name: TextView = v.findViewById(R.id.name)

    }

    class SectionViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var title_section: TextView = v.findViewById(R.id.title_section)

    }
}
