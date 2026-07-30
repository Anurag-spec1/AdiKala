package com.devshiv.adikala.view.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.devshiv.adikala.R
import com.devshiv.adikala.databinding.ItemOrderBinding
import com.devshiv.adikala.model.Order
import com.devshiv.adikala.model.OrderStatus

class OrderAdapter(
    private var list: List<Order>,
    private val onCardClick: ((Order) -> Unit)? = null,
    private val onPrimaryClick: ((Order) -> Unit)? = null,
    private val onSecondaryClick: ((Order) -> Unit)? = null
) : RecyclerView.Adapter<OrderAdapter.ViewHolder>() {

    fun updateList(newList: List<Order>) {
        list = newList
        notifyDataSetChanged()
    }

    private val activeColor = Color.parseColor("#0D3238")
    private val pendingColor = Color.parseColor("#E0D8D0")

    inner class ViewHolder(val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Order) {
            binding.tvOrderId.text = "Order #${item.orderId}"
            binding.tvProductName.text = item.name
            binding.tvItemCount.text = if (item.quantity == 1) "1 item" else "${item.quantity} items"
            binding.tvPrice.text = "₹${item.price}"

            Glide.with(binding.imgOrder)
                .load(item.imageUrl)
                .transition(DrawableTransitionOptions.withCrossFade(200))
                .centerCrop()
                .into(binding.imgOrder)

            applyFinalState(item)
            applyTracker(item)
            applyDeliveryStrip(item)
            applyActions(item)

            binding.cardOrder.setOnClickListener { onCardClick?.invoke(item) }
            binding.btnPrimary.setOnClickListener { onPrimaryClick?.invoke(item) }
            binding.btnSecondary.setOnClickListener { onSecondaryClick?.invoke(item) }
        }

        private fun applyFinalState(item: Order) {
            binding.tvStatusBadge.text = item.status.label

            val (bg, fg) = when (item.status) {
                OrderStatus.DELIVERED -> "#E7F0EA" to "#4A6B5A"
                OrderStatus.CANCELLED -> "#F5E1E1" to "#B04A4A"
                OrderStatus.RETURNED -> "#F5E1E1" to "#B04A4A"
                OrderStatus.OUT_FOR_DELIVERY -> "#FBEADB" to "#C97B4A"
                OrderStatus.SHIPPED -> "#D4E6F1" to "#2E6B9E"
                OrderStatus.PROCESSING -> "#E8E0F0" to "#6B4A8A"
                else -> "#E4EDF2" to "#3F6076"
            }
            binding.statusBadge.setBackgroundColor(Color.parseColor(bg))
            binding.statusDot.setBackgroundColor(Color.parseColor(fg))
            binding.tvStatusBadge.setTextColor(Color.parseColor(fg))
        }

        private fun applyDeliveryStrip(item: Order) {
            if (!item.isOngoing) {
                binding.deliveryStrip.visibility = View.GONE
                return
            }
            when {
                item.status == OrderStatus.DELIVERED -> {
                    binding.deliveryStrip.visibility = View.VISIBLE
                    binding.tvDeliveryInfo.text = if (item.deliveredOn.isNotBlank()) "Delivered on ${item.deliveredOn}" else "Delivered"
                }
                item.status == OrderStatus.CANCELLED || item.status == OrderStatus.RETURNED -> {
                    binding.deliveryStrip.visibility = View.GONE
                }
                item.isOngoing -> {
                    binding.deliveryStrip.visibility = View.VISIBLE
                    binding.tvDeliveryInfo.text = if (item.estimatedDelivery.isNotBlank()) "Estimated: ${item.estimatedDelivery}" else "Awaiting carrier update"
                }
                else -> {
                    binding.deliveryStrip.visibility = View.GONE
                }
            }
        }

        private fun applyTracker(item: Order) {
            binding.progressTracker.visibility = View.GONE
            binding.progressLabels.visibility = View.GONE
        }

        private fun applyActions(item: Order) {
            if (!item.isOngoing) {
                binding.btnPrimary.visibility = View.GONE
                binding.btnSecondary.visibility = View.GONE
                return
            }
            binding.btnPrimary.visibility = View.VISIBLE

            binding.btnSecondary.visibility = View.VISIBLE
            binding.tvSecondaryAction.text = "View Details"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(list[position])

    override fun getItemCount() = list.size
}