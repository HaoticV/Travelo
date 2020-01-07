package com.example.travelo.map

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.travelo.R
import com.example.travelo.models.Rating
import com.mikhaellopez.circularimageview.CircularImageView

class CommentsRecyclerViewAdapter(val contex: Context, private val ratings: ArrayList<Rating>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentsVH(v)
    }

    override fun getItemCount(): Int {
        return ratings.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item: CommentsVH = holder as CommentsVH
        item.name.text = ratings[position].username
        Glide.with(contex).load(ratings[position].userimage).into(item.image)
        item.rating.rating = ratings[position].rating.toFloat()
        item.text.text = ratings[position].text

    }

    class CommentsVH(v: View) : RecyclerView.ViewHolder(v) {
        var name: TextView = v.findViewById(R.id.comment_item_name)
        var image: CircularImageView = v.findViewById(R.id.comment_item_image)
        var rating: AppCompatRatingBar = v.findViewById(R.id.comment_item_rating)
        var text: TextView = v.findViewById(R.id.comment_item_text)
    }
}
