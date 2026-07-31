package com.devshiv.adikala.view.fragment

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.devshiv.adikala.R
import com.devshiv.adikala.databinding.FragmentOrdersBinding
import com.devshiv.adikala.model.Order
import com.devshiv.adikala.model.OrderStatus
import com.devshiv.adikala.view.adapter.OrderAdapter
import com.devshiv.adikala.viewmodel.OrdersViewModel

class OrdersFragment : Fragment() {

    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: OrdersViewModel
    private lateinit var adapter: OrderAdapter
    private var showingOngoing = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[OrdersViewModel::class.java]

        adapter = OrderAdapter(
            emptyList(),
            onPrimaryClick = { order ->
                val phone = "918130507350"
                val message = "Hi, I need support for Order #${order.orderId}"
                val url = "https://wa.me/$phone?text=${android.net.Uri.encode(message)}"
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                    startActivity(intent)
                } catch (e: Exception) {
                    android.widget.Toast.makeText(requireContext(), "WhatsApp not installed", android.widget.Toast.LENGTH_SHORT).show()
                }
            },
            onSecondaryClick = { order ->
                showOrderDetailsDialog(order)
            }
        )
        binding.btnHelp.setOnClickListener {
            val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO)
            intent.data = android.net.Uri.parse("mailto:contact@adikala.in")
            intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Order Support")
            try {
                startActivity(intent)
            } catch (e: Exception) {
                android.widget.Toast.makeText(requireContext(), "No email app found", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        binding.rvOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOrders.adapter = adapter

        setupTabs()
        observeViewModel()
        viewModel.loadOrders()
    }

    private fun setupTabs() {
        binding.tabOngoing.setOnClickListener {
            showingOngoing = true
            updateTabStyles()
            render()
        }
        binding.tabPast.setOnClickListener {
            showingOngoing = false
            updateTabStyles()
            render()
        }
    }

    private fun observeViewModel() {
        viewModel.ongoingOrders.observe(viewLifecycleOwner) { updateTabStyles(); render() }
        viewModel.pastOrders.observe(viewLifecycleOwner) { updateTabStyles(); render() }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                binding.emptyState.visibility = View.VISIBLE
                binding.rvOrders.visibility = View.GONE
            }
        }
    }

    private fun updateTabStyles() {
        val ongoingCount = viewModel.ongoingOrders.value?.size ?: 0
        val pastCount = viewModel.pastOrders.value?.size ?: 0

        binding.tvCountOngoing.text = ongoingCount.toString()
        binding.tvCountPast.text = pastCount.toString()

        val teal = android.graphics.Color.parseColor("#0D3238")
        val white = android.graphics.Color.WHITE
        val muted = android.graphics.Color.parseColor("#8A8A8A")

        if (showingOngoing) {
            binding.tabOngoing.setBackgroundColor(teal)
            binding.tvTabOngoing.setTextColor(white)
            binding.tvCountOngoing.setTextColor(white)
            binding.tabPast.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            binding.tvTabPast.setTextColor(muted)
            binding.tvCountPast.setTextColor(muted)
        } else {
            binding.tabPast.setBackgroundColor(teal)
            binding.tvTabPast.setTextColor(white)
            binding.tvCountPast.setTextColor(white)
            binding.tabOngoing.elevation = 0f
            binding.tabOngoing.setBackgroundColor(android.graphics.Color.WHITE)
            binding.tvTabOngoing.setTextColor(muted)
            binding.tvCountOngoing.setTextColor(muted)
        }
    }

    private fun showOrderDetailsDialog(order: Order) {
        val view = layoutInflater.inflate(R.layout.dialog_order_details, null)

        val imgProduct = view.findViewById<carbon.widget.ImageView>(R.id.imgProduct)
        val tvDialogOrderId = view.findViewById<carbon.widget.TextView>(R.id.tvDialogOrderId)
        val tvDialogStatus = view.findViewById<carbon.widget.TextView>(R.id.tvDialogStatus)
        val statusBadge = view.findViewById<carbon.widget.LinearLayout>(R.id.statusBadge)
        val statusDot = view.findViewById<carbon.widget.LinearLayout>(R.id.statusDot)
        val tvDialogDate = view.findViewById<carbon.widget.TextView>(R.id.tvDialogDate)
        val tvProductName = view.findViewById<carbon.widget.TextView>(R.id.tvProductName)
        val tvItemQty = view.findViewById<carbon.widget.TextView>(R.id.tvItemQty)
        val tvItemPrice = view.findViewById<carbon.widget.TextView>(R.id.tvItemPrice)
        val tvSubtotal = view.findViewById<carbon.widget.TextView>(R.id.tvSubtotal)
        val tvTax = view.findViewById<carbon.widget.TextView>(R.id.tvTax)
        val tvTotal = view.findViewById<carbon.widget.TextView>(R.id.tvTotal)
        val tvEmail = view.findViewById<carbon.widget.TextView>(R.id.tvEmail)
        val tvShippingCost = view.findViewById<carbon.widget.TextView>(R.id.tvShippingCost)
        val tvShippingAddress = view.findViewById<carbon.widget.TextView>(R.id.tvShippingAddress)
        val tvPaymentMethod = view.findViewById<carbon.widget.TextView>(R.id.tvPaymentMethod)
        val btnClose = view.findViewById<View>(R.id.btnClose)

        Glide.with(requireContext()).load(order.imageUrl).centerCrop().into(imgProduct)
        tvDialogOrderId.text = "Order #${order.orderId}"
        tvDialogStatus.text = order.status.label
        tvDialogDate.text = order.date
        tvProductName.text = order.name
        tvItemQty.text = "Qty: ${order.quantity}"

        val totalPrice = order.price.toDouble()
        val taxAmount = order.totalTax?.toDoubleOrNull() ?: 0.0
        val subtotal = totalPrice - taxAmount
        val shipping = if (totalPrice > 0) totalPrice - subtotal - taxAmount else 80.0

        tvItemPrice.text = "₹${subtotal.toInt()}"
        tvSubtotal.text = "₹${String.format("%.2f", subtotal)}"
        tvShippingCost.text = "₹${String.format("%.2f", shipping)}"
        tvTax.text = "₹${order.totalTax ?: "0.00"}"
        tvTotal.text = "₹${order.price} INR"
        tvEmail.text = order.email ?: "N/A"
        tvShippingAddress.text = order.shippingAddress ?: "N/A"
        tvPaymentMethod.text = order.paymentMethod ?: "N/A"

        val (bg, fg) = when (order.status) {
            OrderStatus.DELIVERED -> "#E7F0EA" to "#4A6B5A"
            OrderStatus.CANCELLED -> "#F5E1E1" to "#B04A4A"
            OrderStatus.PROCESSING -> "#E8E0F0" to "#6B4A8A"
            else -> "#E4EDF2" to "#3F6076"
        }
        statusBadge.setBackgroundColor(Color.parseColor(bg))
        statusDot.setBackgroundColor(Color.parseColor(fg))
        tvDialogStatus.setTextColor(Color.parseColor(fg))

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        btnClose.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun render() {
        val orders = if (showingOngoing) {
            viewModel.ongoingOrders.value ?: emptyList()
        } else {
            viewModel.pastOrders.value ?: emptyList()
        }

        if (orders.isEmpty()) {
            binding.rvOrders.visibility = View.GONE
            binding.emptyState.visibility = View.VISIBLE
            binding.tvEmptyTitle.text = if (showingOngoing) "No ongoing orders" else "No past orders"
            binding.tvEmptySubtitle.text = if (showingOngoing) "Your active orders will show up here" else "Delivered and completed orders appear here"
            return
        }

        binding.rvOrders.visibility = View.VISIBLE
        binding.emptyState.visibility = View.GONE

        val displayOrders = orders.map { order ->
            val firstItem = order.items.firstOrNull()
            val shippingAddr = order.shippingAddress
            val addressStr = if (shippingAddr != null) {
                listOfNotNull(
                    shippingAddr.name,
                    shippingAddr.address1,
                    shippingAddr.address2,
                    "${shippingAddr.city} ${shippingAddr.province} ${shippingAddr.zip}",
                    shippingAddr.country
                ).joinToString("\n")
            } else null

            Order(
                orderId = order.name.removePrefix("#"),
                name = firstItem?.title ?: "Order",
                imageUrl = firstItem?.imageUrl ?: "",
                quantity = order.items.sumOf { it.quantity },
                price = order.totalPrice.replace(Regex("[^0-9.]"), "").toDoubleOrNull()?.toInt() ?: 0,
                date = order.processedAt?.take(10) ?: "",
                status = when {
                    order.fulfillmentStatus == "FULFILLED" -> OrderStatus.DELIVERED
                    order.financialStatus == "CANCELLED" || order.financialStatus == "REFUNDED" -> OrderStatus.CANCELLED
                    order.fulfillmentStatus == "IN_PROGRESS" || order.financialStatus == "PAID" -> OrderStatus.PROCESSING
                    else -> OrderStatus.CONFIRMED
                },
                estimatedDelivery = order.trackingInfo?.estimatedDelivery ?: "",
                trackingNumber = order.trackingInfo?.number,
                trackingUrl = order.trackingInfo?.url,
                isOngoing = showingOngoing,
                showTracker = false,
                showTrackButton = false,
                email = order.email,
                shippingAddress = addressStr,
                totalTax = order.totalTax,
                paymentMethod = if (order.financialStatus == "PENDING") "Cash On Delivery" else "Prepaid"
            )
        }
        adapter.updateList(displayOrders)
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadOrders()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}