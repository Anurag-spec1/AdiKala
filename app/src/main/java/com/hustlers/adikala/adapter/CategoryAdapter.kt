package com.hustlers.adikala.adapter

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hustlers.adikala.databinding.ItemCategoryBinding
import com.hustlers.adikala.dataclass.Category

class CategoryAdapter(
    private val list: List<Category>,
    private val onClick: ((Category) -> Unit)? = null
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    inner class ViewHolder(
        private val binding: ItemCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Category) {
            binding.root.contentDescription = "Open ${item.name} category"
            binding.tvChipLabel.text = item.name

            Glide.with(binding.ivCategory)
                .load(item.image)
                .centerCrop()
                .into(binding.ivCategory)

            binding.root.setOnClickListener { onClick?.invoke(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            ItemCategoryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(list[position])

    override fun getItemCount() = list.size
}