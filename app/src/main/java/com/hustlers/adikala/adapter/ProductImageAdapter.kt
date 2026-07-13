package com.hustlers.adikala.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hustlers.adikala.databinding.ItemProductImageBinding

class ProductImageAdapter(
    private val images: List<String>
) : RecyclerView.Adapter<ProductImageAdapter.VH>() {

    inner class VH(val b: ItemProductImageBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(url: String) {
            Glide.with(b.imgSlide).load(url).centerCrop().into(b.imgSlide)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemProductImageBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(images[position])
    override fun getItemCount() = images.size
}