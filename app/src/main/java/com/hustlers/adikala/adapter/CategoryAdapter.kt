package com.hustlers.adikala.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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

            binding.tvChipLabel.text = item.name

            binding.root.contentDescription = "Open ${item.name} category"

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