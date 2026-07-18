package com.hustlers.adikala.view.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.hustlers.adikala.R
import com.hustlers.adikala.databinding.ItemOrderBinding
import com.hustlers.adikala.model.Order
import com.hustlers.adikala.model.OrderStatus

class OrderAdapter(
    private val list: List<Order>,
    private val onCardClick: ((Order) -> Unit)? = null,
    private val onPrimaryClick: ((Order) -> Unit)? = null,
    private val onSecondaryClick: ((Order) -> Unit)? = null
) : RecyclerView.Adapter<OrderAdapter.ViewHolder>() {

    private val done = Color.parseColor("#0D3238")
    private val pending = Color.parseColor("#E0D8D0")

    inner class ViewHolder(val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Order) {
            binding.tvOrderId.text = "Order #${item.orderId}"
            binding.tvProductName.text = item.productName
            binding.tvItemCount.text =
                if (item.itemCount == 1) "1 item" else "${item.itemCount} items"
            binding.tvPrice.text = "₹${item.totalPrice}"

            Glide.with(binding.imgOrder)
                .load(item.imageUrl)
                .transition(DrawableTransitionOptions.withCrossFade(200))
                .centerCrop()
                .into(binding.imgOrder)

            applyStatus(item)
            applyProgress(item)
            applyDeliveryStrip(item)
            applyActions(item)

            binding.cardOrder.setOnClickListener { onCardClick?.invoke(item) }
            binding.btnPrimary.setOnClickListener { onPrimaryClick?.invoke(item) }
            binding.btnSecondary.setOnClickListener { onSecondaryClick?.invoke(item) }
        }

        private fun applyStatus(item: Order) {
            binding.tvStatusBadge.text = item.status.label

            val (bg, fg) = when (item.status) {
                OrderStatus.DELIVERED -> "#E7F0EA" to "#4A6B5A"
                OrderStatus.CANCELLED -> "#F5E1E1" to "#B04A4A"
                OrderStatus.RETURNED -> "#F5E1E1" to "#B04A4A"
                OrderStatus.OUT_FOR_DELIVERY -> "#FBEADB" to "#C97B4A"
                else -> "#E4EDF2" to "#3F6076"
            }
            binding.statusBadge.setBackgroundColor(Color.parseColor(bg))
            binding.statusDot.setBackgroundColor(Color.parseColor(fg))
            binding.tvStatusBadge.setTextColor(Color.parseColor(fg))
        }

        private fun applyProgress(item: Order) {
            if (!item.isOngoing) {
                binding.progressTracker.visibility = View.GONE
                binding.progressLabels.visibility = View.GONE
                return
            }
            binding.progressTracker.visibility = View.VISIBLE
            binding.progressLabels.visibility = View.VISIBLE

            val active = when (item.status) {
                OrderStatus.PLACED -> 0
                OrderStatus.PACKED -> 1
                OrderStatus.SHIPPED -> 2
                OrderStatus.OUT_FOR_DELIVERY -> 3
                else -> 0
            }

            val dots = listOf(binding.dot1, binding.dot2, binding.dot3, binding.dot4)
            val lines = listOf(binding.line1, binding.line2, binding.line3)

            dots.forEachIndexed { i, dot ->
                dot.setBackgroundColor(if (i <= active) done else pending)
            }
            lines.forEachIndexed { i, line ->
                line.setBackgroundColor(if (i < active) done else pending)
            }
        }

        private fun applyDeliveryStrip(item: Order) {
            when (item.status) {
                OrderStatus.DELIVERED -> {
                    binding.deliveryStrip.visibility = View.VISIBLE
                    binding.tvDeliveryInfo.text =
                        if (item.deliveredOn.isNotBlank()) "Delivered on ${item.deliveredOn}"
                        else "Delivered"
                }
                OrderStatus.CANCELLED -> binding.deliveryStrip.visibility = View.GONE
                OrderStatus.RETURNED -> {
                    binding.deliveryStrip.visibility = View.VISIBLE
                    binding.tvDeliveryInfo.text = "Return completed"
                }
                else -> {
                    if (item.expectedDelivery.isNotBlank()) {
                        binding.deliveryStrip.visibility = View.VISIBLE
                        binding.tvDeliveryInfo.text = "Arriving by ${item.expectedDelivery}"
                    } else {
                        binding.deliveryStrip.visibility = View.GONE
                    }
                }
            }
        }

        private fun applyActions(item: Order) {
            if (item.isOngoing) {
                binding.ivPrimaryIcon.setImageResource(R.drawable.ic_truck)
                binding.tvPrimaryAction.text = "Track Order"
                binding.tvSecondaryAction.text = "View Details"
            } else {
                binding.ivPrimaryIcon.setImageResource(R.drawable.ic_repeat)
                binding.tvPrimaryAction.text = "Buy Again"
                binding.tvSecondaryAction.text =
                    if (item.status == OrderStatus.DELIVERED) "Rate" else "View Details"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(list[position])

    override fun getItemCount() = list.size
}