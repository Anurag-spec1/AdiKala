package com.hustlers.adikala.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.flexbox.FlexboxLayout
import androidx.recyclerview.widget.RecyclerView
import com.hustlers.adikala.databinding.ItemCategoryGridBinding
import com.hustlers.adikala.databinding.ItemSubcategoryChipBinding
import com.hustlers.adikala.dataclass.Category
import com.hustlers.adikala.dataclass.SubCategory

class CategoryGridAdapter(
    private val list: List<Category>,
    private val onCategoryClick: ((Category) -> Unit)? = null,
    private val onSubCategoryClick: ((Category, SubCategory) -> Unit)? = null
) : RecyclerView.Adapter<CategoryGridAdapter.ViewHolder>() {

    private val maxVisibleChips = 4

    inner class ViewHolder(
        val binding: ItemCategoryGridBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Category) {
            binding.tvChipLabel.text = item.name

            if (item.itemCount > 0) {
                binding.tvCount.visibility = View.VISIBLE
                binding.tvCount.text =
                    if (item.itemCount == 1) "1 item" else "${item.itemCount} items"
            } else {
                binding.tvCount.visibility = View.GONE
            }

            val subs = item.subCategories
            val subCount = subs.size
            if (subCount > 0) {
                binding.tvSubCount.visibility = View.VISIBLE
                binding.tvSubCount.text =
                    if (subCount == 1) "1 subcategory" else "$subCount subcategories"
                binding.flexSubChips.visibility = View.VISIBLE
                buildChips(item)
            } else {
                binding.tvSubCount.visibility = View.GONE
                binding.flexSubChips.visibility = View.GONE
            }

            Glide.with(binding.ivCategory)
                .load(item.image)
                .transition(DrawableTransitionOptions.withCrossFade(220))
                .centerCrop()
                .into(binding.ivCategory)

            binding.cardCategory.setOnClickListener { onCategoryClick?.invoke(item) }
        }

        private fun buildChips(item: Category) {
            val flex = binding.flexSubChips
            flex.removeAllViews()

            val subs = item.subCategories
            val showMore = subs.size > maxVisibleChips
            val visibleCount = if (showMore) maxVisibleChips else subs.size
            val inflater = LayoutInflater.from(flex.context)

            for (i in 0 until visibleCount) {
                val sub = subs[i]
                val chip = ItemSubcategoryChipBinding.inflate(inflater, flex, false)
                chip.tvSubChip.text = sub.name
                chip.tvSubChip.setBackgroundColor(Color.parseColor("#F2ECE6"))
                chip.tvSubChip.setTextColor(Color.parseColor("#0D3238"))
                chip.tvSubChip.setOnClickListener { onSubCategoryClick?.invoke(item, sub) }
                flex.addView(chip.root)
            }

            if (showMore) {
                val remaining = subs.size - maxVisibleChips
                val more = ItemSubcategoryChipBinding.inflate(inflater, flex, false)
                more.tvSubChip.text = "+$remaining more"
                more.tvSubChip.setBackgroundColor(Color.parseColor("#0D3238"))
                more.tvSubChip.setTextColor(Color.WHITE)
                more.tvSubChip.setOnClickListener { onCategoryClick?.invoke(item) }
                flex.addView(more.root)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryGridBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(list[position])

    override fun getItemCount() = list.size
}