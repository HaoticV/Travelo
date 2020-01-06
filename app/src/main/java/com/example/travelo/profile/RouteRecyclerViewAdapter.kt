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

    private val ROUTE_VIEW = 1
    private val SECTION_VIEW = 0
    private var lastPosition = -1
    private var mOnItemClickListener: OnItemClickListener? = null
    private val on_attach = true

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
            val route = items[position] as Route
            item.name.text = route.name
            item.description.text = route.description
            item.route_distance.text = route.distanceText
            item.route_time.text = route.timeText

            when (route.type) {
                "mountain" -> item.image.setImageResource(R.drawable.ic_cyclist_mountain)
                "city" -> item.image.setImageResource(R.drawable.ic_cyclist_city)
                "road" -> item.image.setImageResource(R.drawable.ic_cyclist_road)
            }
            holder.itemView.setOnClickListener {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener!!.onItemClick(holder.itemView, route, position)
                }
            }
        } else {
            val view: SectionViewHolder = holder as SectionViewHolder
            view.sectionTitle.text = items[position].toString()
        }
        setAnimation(holder.itemView, position)
    }

    interface OnItemClickListener {
        fun onItemClick(view: View?, obj: Route?, position: Int)
    }

    fun setOnItemClickListener(mItemClickListener: OnItemClickListener) {
        this.mOnItemClickListener = mItemClickListener
    }

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
        var sectionTitle: TextView = v.findViewById(R.id.title_section)

    }
}
