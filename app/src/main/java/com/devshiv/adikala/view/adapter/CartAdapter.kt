package com.devshiv.adikala.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devshiv.adikala.databinding.ItemCartBinding
import com.devshiv.adikala.viewmodel.CartViewModel.CartItemDisplay

class CartAdapter(
    private val onQuantityChanged: (lineId: String, quantity: Int) -> Unit,
    private val onRemove: (lineId: String, variantId: String) -> Unit
) : ListAdapter<CartItemDisplay, CartAdapter.VH>(DiffCallback) {

    class VH(val b: ItemCartBinding) : RecyclerView.ViewHolder(b.root)

    object DiffCallback : DiffUtil.ItemCallback<CartItemDisplay>() {
        override fun areItemsTheSame(old: CartItemDisplay, new: CartItemDisplay) = old.lineId == new.lineId
        override fun areContentsTheSame(old: CartItemDisplay, new: CartItemDisplay) = old == new
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        with(holder.b) {
            tvName.text = item.name
            tvQty.text = item.quantity.toString()
            tvPrice.text = item.price

            if (!item.size.isNullOrBlank()) {
                tvSize.visibility = View.VISIBLE
                tvSize.text = item.size
            } else {
                tvSize.visibility = View.GONE
            }

            btnMinus.alpha = if (item.quantity > 1) 1f else 0.35f

            Glide.with(imgItem).load(item.imageUrl).centerCrop().into(imgItem)

            btnPlus.setOnClickListener {
                val newQty = item.quantity + 1
                if (newQty <= 99) onQuantityChanged(item.lineId, newQty)
            }
            btnMinus.setOnClickListener {
                if (item.quantity > 1) onQuantityChanged(item.lineId, item.quantity - 1)
            }
            btnRemove.setOnClickListener { onRemove(item.lineId, item.variantId) }
        }
    }
}