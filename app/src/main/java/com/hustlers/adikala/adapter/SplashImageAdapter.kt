package com.hustlers.adikala.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hustlers.adikala.databinding.ItemSplashImageBinding

class SplashImageAdapter(
    private val images: List<Int>,
    private val itemHeightPx: Int,
    private val gapPx: Int
) : RecyclerView.Adapter<SplashImageAdapter.VH>() {

    inner class VH(val b: ItemSplashImageBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(resId: Int) {
            val lp = b.ivSplash.layoutParams as ViewGroup.MarginLayoutParams
            lp.height = itemHeightPx
            lp.bottomMargin = gapPx
            b.ivSplash.layoutParams = lp

            Glide.with(b.ivSplash)
                .load(resId)
                .centerCrop()
                .into(b.ivSplash)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemSplashImageBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(images[position % images.size])

    override fun getItemCount() = Int.MAX_VALUE
}