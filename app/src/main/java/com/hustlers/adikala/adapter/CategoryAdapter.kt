package com.hustlers.adikala.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hustlers.adikala.databinding.ItemCategoryBinding
import com.hustlers.adikala.dataclass.Category

class CategoryAdapter(
    private val list: List<Category>,
    private val onClick: ((Category) -> Unit)? = null
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private val avatarColors = listOf(
        Color.parseColor("#0D3B3E"), // deep teal
        Color.parseColor("#C97B4A"), // terracotta
        Color.parseColor("#B08D57"), // muted gold
        Color.parseColor("#6B8E6B"), // sage green
        Color.parseColor("#8C6E8C"), // dusty plum
        Color.parseColor("#5B7C99"), // dusty blue
        Color.parseColor("#C2707D"), // dusty rose
        Color.parseColor("#4A6B5A")  // forest green
    )

    inner class ViewHolder(
        private val binding: ItemCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Category) {

            binding.root.contentDescription = "Open ${item.name} category"

            binding.tvChipLabel.text = item.name
            binding.tvAvatarLetter.text = item.name.take(1).uppercase()

            val color = avatarColors[item.name.hashCode().mod(avatarColors.size)]
            binding.iconContainer.stroke = ColorStateList.valueOf(color)
            binding.tvAvatarLetter.setTextColor(color)

            binding.root.setOnClickListener {
                onClick?.invoke(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ItemCategoryBinding.inflate(
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