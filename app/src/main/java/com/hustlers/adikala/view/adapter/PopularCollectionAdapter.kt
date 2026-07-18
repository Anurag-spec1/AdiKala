package com.hustlers.adikala.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hustlers.adikala.databinding.ItemPopularCollectionBinding
import com.hustlers.adikala.model.PopularCollection

class PopularCollectionAdapter(
    private val list: List<PopularCollection>,
    private val onClick: ((PopularCollection) -> Unit)? = null
) : RecyclerView.Adapter<PopularCollectionAdapter.ViewHolder>() {

    inner class ViewHolder(
        val binding: ItemPopularCollectionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PopularCollection) {
            binding.tvTitle.text = item.name
            Glide.with(binding.root).load(item.image).into(binding.imgProduct)
            binding.root.setOnClickListener { onClick?.invoke(item) }
            binding.glowBorder.startGlowAnimation()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPopularCollectionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(list[position])

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.binding.glowBorder.stopGlowAnimation()
    }

    override fun getItemCount() = list.size
}