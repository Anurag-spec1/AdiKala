package com.devshiv.adikala.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.devshiv.adikala.databinding.ItemSubcategoryGridBinding
import com.devshiv.adikala.model.SubCategory

class SubCategoryGridAdapter(
    private val list: List<SubCategory>,
    private val onClick: ((SubCategory) -> Unit)? = null
) : RecyclerView.Adapter<SubCategoryGridAdapter.ViewHolder>() {

    inner class ViewHolder(
        val binding: ItemSubcategoryGridBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SubCategory) {
            binding.tvSubName.text = item.name

            if (item.itemCount > 0) {
                binding.tvSubItems.visibility = View.VISIBLE
                binding.tvSubItems.text =
                    if (item.itemCount == 1) "1 item" else "${item.itemCount} items"
            } else {
                binding.tvSubItems.visibility = View.GONE
            }

            Glide.with(binding.ivSub)
                .load(item.image)
                .transition(DrawableTransitionOptions.withCrossFade(220))
                .centerCrop()
                .into(binding.ivSub)

            binding.cardSub.setOnClickListener { onClick?.invoke(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSubcategoryGridBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(list[position])

    override fun getItemCount() = list.size
}