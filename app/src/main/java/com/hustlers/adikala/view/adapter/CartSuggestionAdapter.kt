package com.hustlers.adikala.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hustlers.adikala.databinding.ItemCartSuggestionBinding
import com.hustlers.adikala.model.Product

class CartSuggestionAdapter(
    private val list: List<Product>,
    private val onAdd: (Product) -> Unit,
    private val onClick: (Product) -> Unit
) : RecyclerView.Adapter<CartSuggestionAdapter.VH>() {

    inner class VH(val b: ItemCartSuggestionBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: Product) {
            b.tvSuggestName.text = item.name
            b.tvSuggestPrice.text = "₹${item.discountedPrice}"

            Glide.with(b.imgSuggest).load(item.imageUrl).centerCrop().into(b.imgSuggest)

            b.cardSuggest.setOnClickListener { onClick(item) }
            b.btnAddSuggest.setOnClickListener { onAdd(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemCartSuggestionBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(list[position])
    override fun getItemCount() = list.size
}