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
    private val list: List<Order>,
    private val onCardClick: ((Order) -> Unit)? = null,
    private val onPrimaryClick: ((Order) -> Unit)? = null,
    private val onSecondaryClick: ((Order) -> Unit)? = null
) : RecyclerView.Adapter<OrderAdapter.ViewHolder>() {

    private val activeColor = Color.parseColor("#0D3238")
    private val pendingColor = Color.parseColor("#E0D8D0")
    private val pausedColor = Color.parseColor("#D4A854")
    private val partialColor = Color.parseColor("#8BA4B8")

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

        private fun applyTracker(item: Order) {
            if (!item.showTracker) {
                binding.progressTracker.visibility = View.GONE
                binding.progressLabels.visibility = View.GONE
                return
            }
            binding.progressTracker.visibility = View.VISIBLE
            binding.progressLabels.visibility = View.VISIBLE

            val step = item.progressStep.coerceIn(0, 3)
            val dots = listOf(binding.dot1, binding.dot2, binding.dot3, binding.dot4)
            val lines = listOf(binding.line1, binding.line2, binding.line3)

            if (item.isPaused) {
                dots.forEachIndexed { i, dot ->
                    dot.setBackgroundColor(if (i <= step) pausedColor else pendingColor)
                }
                lines.forEachIndexed { i, line ->
                    line.setBackgroundColor(if (i < step) pausedColor else pendingColor)
                }
            } else {
                dots.forEachIndexed { i, dot ->
                    dot.setBackgroundColor(if (i <= step) activeColor else pendingColor)
                }
                lines.forEachIndexed { i, line ->
                    line.setBackgroundColor(if (i < step) activeColor else pendingColor)
                }
            }
        }

        private fun applyDeliveryStrip(item: Order) {
            when {
                item.status == OrderStatus.DELIVERED -> {
                    binding.deliveryStrip.visibility = View.VISIBLE
                    binding.tvDeliveryInfo.text = if (item.deliveredOn.isNotBlank()) "Delivered on ${item.deliveredOn}" else "Delivered"
                }
                item.status == OrderStatus.CANCELLED || item.status == OrderStatus.RETURNED -> {
                    binding.deliveryStrip.visibility = View.GONE
                }
                item.isOngoing && item.showTrackButton -> {
                    binding.deliveryStrip.visibility = View.VISIBLE
                    binding.tvDeliveryInfo.text = if (item.estimatedDelivery.isNotBlank()) "Arriving by ${item.estimatedDelivery}" else "Tracking available"
                }
                item.isOngoing && !item.showTrackButton -> {
                    binding.deliveryStrip.visibility = View.VISIBLE
                    binding.tvDeliveryInfo.text = if (item.estimatedDelivery.isNotBlank()) "Estimated: ${item.estimatedDelivery}" else "Awaiting carrier update"
                }
                else -> {
                    binding.deliveryStrip.visibility = View.GONE
                }
            }
        }

        private fun applyActions(item: Order) {
            if (item.showTrackButton) {
                binding.ivPrimaryIcon.setImageResource(R.drawable.ic_truck)
                binding.tvPrimaryAction.text = "Track Order"
                binding.tvSecondaryAction.text = "View Details"
            } else if (item.isOngoing) {
                binding.ivPrimaryIcon.setImageResource(R.drawable.ic_help)
                binding.tvPrimaryAction.text = "Contact Support"
                binding.tvSecondaryAction.text = "View Details"
            } else {
                binding.ivPrimaryIcon.setImageResource(R.drawable.ic_repeat)
                binding.tvPrimaryAction.text = "Buy Again"
                binding.tvSecondaryAction.text = if (item.status == OrderStatus.DELIVERED) "Rate" else "View Details"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(list[position])

    override fun getItemCount() = list.size
}