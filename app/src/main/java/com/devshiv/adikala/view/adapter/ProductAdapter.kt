package com.devshiv.adikala.view.adapter

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devshiv.adikala.R
import com.devshiv.adikala.databinding.ItemProductsBinding
import com.devshiv.adikala.model.Product

class ProductAdapter(
    private var list: List<Product>,
    private val onClick: ((Product) -> Unit)? = null,
    private val onAddToCart: ((Product) -> Unit)? = null,
    private val onWishlistToggle: ((Product) -> Unit)? = null
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    fun updateList(newList: List<Product>) {
        list = newList
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        val binding: ItemProductsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Product) {
            binding.tvDescription.text = item.description

            // Formatted rating; show "New" when the product has no reviews yet.
            binding.tvRating.text =
                if (item.rating > 0) String.format("%.1f", item.rating) else "New"

            binding.tvDiscountedPrice.text = "₹${item.discountedPrice}"
            binding.tvActualPrice.text = "₹${item.actualPrice}"

            binding.tvActualPrice.paintFlags =
                binding.tvActualPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

            val onSale = item.discountedPrice < item.actualPrice
            if (onSale) {
                val saved = item.actualPrice - item.discountedPrice
                val percent = saved * 100 / item.actualPrice
                binding.tvDiscountBadge.text = "$percent% OFF"
                binding.tvDiscountBadge.visibility = View.VISIBLE
                binding.tvActualPrice.visibility = View.VISIBLE
            } else {
                binding.tvDiscountBadge.visibility = View.GONE
                binding.tvActualPrice.visibility = View.GONE
            }

            binding.btnWishlist.setImageResource(
                if (item.isWishlisted) R.drawable.ic_heart_filled else R.drawable.ic_heart
            )
            binding.btnWishlist.setColorFilter(
                Color.parseColor(if (item.isWishlisted) "#C2707D" else "#0D3238")
            )

            Glide.with(binding.root).load(item.imageUrl).into(binding.imgProduct)

            binding.root.setOnClickListener { onClick?.invoke(item) }
            binding.btnQuickAdd.setOnClickListener { onAddToCart?.invoke(item) }
            binding.btnWishlist.setOnClickListener { onWishlistToggle?.invoke(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(list[position])

    override fun getItemCount() = list.size
}