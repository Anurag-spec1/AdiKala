package com.devshiv.adikala.view.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devshiv.adikala.R
import com.devshiv.adikala.databinding.ItemThumbnailBinding

class ThumbnailAdapter(
    private val images: List<String>,
    private val onPick: (Int) -> Unit
) : RecyclerView.Adapter<ThumbnailAdapter.VH>() {

    private var selected = 0

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

            val isSelected = pos == selected
            b.ivThumb.alpha = if (isSelected) 1f else 0.55f

            val context = b.ivThumb.context
            val strokeColor = if (isSelected) {
                androidx.core.content.ContextCompat.getColor(context, R.color.teal)
            } else {
                android.graphics.Color.TRANSPARENT
            }
            b.ivThumb.stroke = ColorStateList.valueOf(strokeColor)

            b.ivThumb.setOnClickListener { onPick(pos) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemThumbnailBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(images[position], position)
    override fun getItemCount() = images.size
}