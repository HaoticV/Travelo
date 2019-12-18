package com.example.travelo.map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.travelo.R
import com.smarteist.autoimageslider.SliderViewAdapter

class SliderAdapter(private val images: List<*>) : SliderViewAdapter<SliderAdapter.SliderAdapterVH>() {


    override fun onCreateViewHolder(parent: ViewGroup): SliderAdapterVH? {
        val inflate = LayoutInflater.from(parent.context).inflate(R.layout.image_slider_layout_item, null)
        return SliderAdapterVH(inflate)
    }

    override fun onBindViewHolder(viewHolder: SliderAdapterVH, position: Int) {
        Glide.with(viewHolder.itemView)
            .load(images[position])
            .into(viewHolder.imageViewBackground)
    }

    override fun getCount(): Int {
        return images.size
    }

    class SliderAdapterVH(var itemView: View) : ViewHolder(itemView) {
        var imageViewBackground: ImageView = itemView.findViewById(R.id.iv_auto_image_slider)

    }
}