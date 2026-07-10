package com.hustlers.adikala.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hustlers.adikala.databinding.ItemPopularCollectionBinding
import com.hustlers.adikala.dataclass.PopularCollection

class PopularCollectionAdapter(
    private val list: List<PopularCollection>,
    private val onClick: ((PopularCollection) -> Unit)? = null
) : RecyclerView.Adapter<PopularCollectionAdapter.ViewHolder>() {

    inner class ViewHolder(
        private val binding: ItemPopularCollectionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PopularCollection) {
            binding.tvTag.text = item.name

            Glide.with(binding.root)
                .load(item.image)
                .into(binding.imgCollection)

            binding.swipeButton.buttonText = "Swipe to Explore"
            binding.swipeButton.onSwipeComplete = {
                onClick?.invoke(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ItemPopularCollectionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(list[position])

    }

    override fun getItemCount() = list.size
}