package com.devshiv.adikala.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devshiv.adikala.databinding.ItemCartBinding
import com.devshiv.adikala.model.CartItem

class CartAdapter(
    private val list: MutableList<CartItem>,
    private val onQtyChanged: (CartItem) -> Unit,
    private val onRemove: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            binding.tvName.text = item.name
            binding.tvQty.text = item.quantity.toString()
            binding.tvPrice.text = "₹${item.price * item.quantity}"

            if (item.size.isNotBlank()) {
                binding.tvSize.visibility = View.VISIBLE
                binding.tvSize.text = "Size: ${item.size}"
            } else {
                binding.tvSize.visibility = View.GONE
            }

            binding.btnMinus.alpha = if (item.quantity > 1) 1f else 0.35f

            Glide.with(binding.imgItem).load(item.imageUrl).centerCrop().into(binding.imgItem)

            binding.btnPlus.setOnClickListener {
                if (item.quantity < 99) {
                    item.quantity++
                    notifyItemChanged(bindingAdapterPosition)
                    onQtyChanged(item)
                }
            }
            binding.btnMinus.setOnClickListener {
                if (item.quantity > 1) {
                    item.quantity--
                    notifyItemChanged(bindingAdapterPosition)
                    onQtyChanged(item)
                }
            }
            binding.btnRemove.setOnClickListener { onRemove(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(list[position])

    override fun getItemCount() = list.size
}