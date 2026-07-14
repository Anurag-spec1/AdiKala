package com.hustlers.adikala.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hustlers.adikala.databinding.ItemThumbnailBinding

class ThumbnailAdapter(
    private val images: List<String>,
    private val onPick: (Int) -> Unit
) : RecyclerView.Adapter<ThumbnailAdapter.VH>() {

    private var selected = 0
    private val active = Color.parseColor("#0D3238")
    private val inactive = Color.parseColor("#00000000")

    fun setSelected(pos: Int) {
        if (pos == selected) return
        val old = selected
        selected = pos
        notifyItemChanged(old)
        notifyItemChanged(pos)
    }

    inner class VH(val b: ItemThumbnailBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(url: String, pos: Int) {
            Glide.with(b.ivThumb).load(url).centerCrop().into(b.ivThumb)
            b.ivThumb.stroke = ColorStateList.valueOf(if (pos == selected) active else inactive)
            b.ivThumb.alpha = if (pos == selected) 1f else 0.55f
            b.ivThumb.setOnClickListener { onPick(pos) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemThumbnailBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(images[position], position)
    override fun getItemCount() = images.size
}