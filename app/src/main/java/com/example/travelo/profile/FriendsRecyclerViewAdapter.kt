package com.example.travelo.profile

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.travelo.R
import com.example.travelo.lib.ItemAnimation
import com.example.travelo.models.User
import kotlinx.android.synthetic.main.item_people_chat.view.*

class FriendsRecyclerViewAdapter(val context: Context, private val items: ArrayList<User>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mOnItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_people_chat, parent, false)
        return RouteRecyclerViewAdapterVH(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = holder.itemView
        val user = items[position]
        item.name.text = items[position].displayName
        item.own_routes.text = "Dodane trasy: " + items[position].ownRoutes.count().toString()
        item.liked_routes.text = "Polubione trasy: " + items[position].likedRoutes.count().toString()
        Glide.with(context).load(items[position].image).into(item.image)

        holder.itemView.setOnClickListener {
            if (mOnItemClickListener != null) {
                mOnItemClickListener!!.onItemClick(holder.itemView, user, position)
            }
        }

        setAnimation(holder.itemView, position)
    }

    interface OnItemClickListener {
        fun onItemClick(view: View?, obj: User?, position: Int)
    }

    fun setOnItemClickListener(mItemClickListener: OnItemClickListener) {
        this.mOnItemClickListener = mItemClickListener
    }

    private var lastPosition = -1

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
        var image: ImageView = v.findViewById<View>(R.id.image) as ImageView

    }
}
