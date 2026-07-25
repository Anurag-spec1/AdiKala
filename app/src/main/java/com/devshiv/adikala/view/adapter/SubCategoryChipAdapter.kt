package com.devshiv.adikala.view.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.devshiv.adikala.databinding.ItemSubcategoryChipBinding
import com.devshiv.adikala.model.Category
import com.devshiv.adikala.model.SubCategory

class SubCategoryChipAdapter(
    private val category: Category,
    private val maxVisible: Int,
    private val onSubClick: (Category, SubCategory) -> Unit,
    private val onMoreClick: (Category) -> Unit
) : RecyclerView.Adapter<SubCategoryChipAdapter.VH>() {

    private val subs = category.subCategories
    private val showMore = subs.size > maxVisible
    private val visibleCount = if (showMore) maxVisible else subs.size

    inner class VH(val b: ItemSubcategoryChipBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemSubcategoryChipBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val b = holder.b
        val isMoreChip = showMore && position == visibleCount

        if (isMoreChip) {
            val remaining = subs.size - maxVisible
            b.tvSubChip.text = "+$remaining more"
            b.tvSubChip.setBackgroundColor(Color.parseColor("#0D3238"))
            b.tvSubChip.setTextColor(Color.WHITE)
            b.tvSubChip.setOnClickListener { onMoreClick(category) }
        } else {
            val sub = subs[position]
            b.tvSubChip.text = sub.name
            b.tvSubChip.setBackgroundColor(Color.parseColor("#F2ECE6"))
            b.tvSubChip.setTextColor(Color.parseColor("#0D3238"))
            b.tvSubChip.setOnClickListener { onSubClick(category, sub) }
        }
    }

    override fun getItemCount() = visibleCount + if (showMore) 1 else 0
}