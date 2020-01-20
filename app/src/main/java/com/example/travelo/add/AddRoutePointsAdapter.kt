package com.example.travelo.add

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.travelo.R
import com.google.android.libraries.places.api.model.Place
import java.util.*

class AddRoutePointsAdapter(val context: Context, private val items: ArrayList<Place>) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    private var mOnItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(view: View?, obj: Place?, position: Int)
    }

    fun setOnItemClickListener(mItemClickListener: OnItemClickListener?) {
        mOnItemClickListener = mItemClickListener
    }

    inner class PointsViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var number: TextView = v.findViewById(R.id.point_number)
        var name: TextView = v.findViewById(R.id.point_name)
        var delete: ImageView = v.findViewById(R.id.delete_point)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val vh: RecyclerView.ViewHolder
        val v: View = LayoutInflater.from(parent.context).inflate(R.layout.item_route_point, parent, false)
        vh = PointsViewHolder(v)
        return vh
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        val view: PointsViewHolder = holder as PointsViewHolder
        view.number.text = (position + 1).toString() + "."
        view.name.text = "${item.name}"
        view.delete.setOnClickListener {
            mOnItemClickListener?.onItemClick(it, item, position)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

}