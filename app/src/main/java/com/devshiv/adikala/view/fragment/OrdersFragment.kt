package com.devshiv.adikala.view.fragment

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
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

        binding.rvOrders.layoutManager = LinearLayoutManager(requireContext())

        setupTabs()
        observeViewModel()
    }

    private fun showOrderHelpCard() {
        binding.rvOrders.visibility = View.GONE
        binding.emptyState.visibility = View.GONE
        binding.progressBar.visibility = View.GONE

        binding.helpCard.visibility = View.VISIBLE
    }

    private fun setupTabs() {
        binding.tabOngoing.setOnClickListener {
            if (!showingOngoing) {
                showingOngoing = true
                updateTabStyles()
                render()
            }
        }
        binding.tabPast.setOnClickListener {
            if (showingOngoing) {
                showingOngoing = false
                updateTabStyles()
                render()
            }
        }
        binding.btnHelp.setOnClickListener { showOrderHelpCard() }
        binding.btnShopNow.setOnClickListener {
            val intent = Intent(requireContext(), com.devshiv.adikala.view.activity.MainActivity::class.java)
            intent.putExtra("navigate_to", "home")
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        binding.btnCloseHelp.setOnClickListener {
            binding.helpCard.visibility = View.GONE
            render()
        }
    }

    private fun observeViewModel() {
        viewModel.ongoingOrders.observe(viewLifecycleOwner) { orders ->
            updateTabStyles()
            render()
        }
        viewModel.pastOrders.observe(viewLifecycleOwner) { orders ->
            updateTabStyles()
            render()
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                binding.rvOrders.visibility = View.GONE
                binding.emptyState.visibility = View.VISIBLE
                binding.tvEmptyTitle.text = "No orders"
                binding.tvEmptySubtitle.text = it
            }
        }
    }

    private fun updateTabStyles() {
        val teal = Color.parseColor("#0D3238")
        val transparent = Color.TRANSPARENT
        val white = Color.WHITE
        val muted = Color.parseColor("#8A8A8A")
        val badgeOnTeal = Color.parseColor("#33FFFFFF")
        val badgeOnLight = Color.parseColor("#14000000")

        val ongoingCount = viewModel.ongoingOrders.value?.size ?: 0
        val pastCount = viewModel.pastOrders.value?.size ?: 0

        binding.tvCountOngoing.text = ongoingCount.toString()
        binding.tvCountPast.text = pastCount.toString()

        if (showingOngoing) {
            binding.tabOngoing.setBackgroundColor(teal)
            binding.tvTabOngoing.setTextColor(white)
            binding.tvCountOngoing.setBackgroundColor(badgeOnTeal)
            binding.tvCountOngoing.setTextColor(white)

            binding.tabPast.setBackgroundColor(transparent)
            binding.tvTabPast.setTextColor(muted)
            binding.tvCountPast.setBackgroundColor(badgeOnLight)
            binding.tvCountPast.setTextColor(muted)
        } else {
            binding.tabPast.setBackgroundColor(teal)
            binding.tvTabPast.setTextColor(white)
            binding.tvCountPast.setBackgroundColor(badgeOnTeal)
            binding.tvCountPast.setTextColor(white)

            binding.tabOngoing.setBackgroundColor(white)
            binding.tabOngoing.elevation = 0f
            binding.tvTabOngoing.setTextColor(muted)
            binding.tvCountOngoing.setBackgroundColor(badgeOnLight)
            binding.tvCountOngoing.setTextColor(muted)
        }
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
            binding.tvEmptySubtitle.text = if (showingOngoing) "Your active orders will show up here" else "Delivered and closed orders appear here"
            return
        }

        binding.rvOrders.visibility = View.VISIBLE
        binding.emptyState.visibility = View.GONE

        binding.rvOrders.adapter = OrderAdapter(
            orders.map { order ->
                Order(
                    orderId = "#${order.orderNumber}",
                    name = order.name.ifEmpty { order.items.firstOrNull()?.title ?: "Order" },
                    imageUrl = order.imageUrl ?: "",
                    price = order.totalPrice.replace(Regex("[^0-9.]"), "").toDoubleOrNull()?.toInt() ?: 0,
                    quantity = order.items.sumOf { it.quantity },
                    date = order.date,
                    status = when (order.uiDisplayStatus) {
                        "Confirmed" -> OrderStatus.CONFIRMED
                        "Processing" -> OrderStatus.PROCESSING
                        "Shipped", "Partially Shipped" -> OrderStatus.SHIPPED
                        "Out for Delivery" -> OrderStatus.OUT_FOR_DELIVERY
                        "Delivered" -> OrderStatus.DELIVERED
                        "Cancelled" -> OrderStatus.CANCELLED
                        "Refunded", "Partially Refunded" -> OrderStatus.RETURNED
                        "On Hold", "Scheduled", "Payment Pending" -> OrderStatus.CONFIRMED
                        else -> OrderStatus.CONFIRMED
                    },
                    estimatedDelivery = order.estimatedDelivery ?: "",
                    trackingNumber = order.trackingNumber,
                    trackingUrl = order.trackingUrl,
                    isOngoing = order.isOngoing,
                    progressStep = order.progressStep,
                    showTracker = order.showTracker,
                    showTrackButton = order.showTrackButton,
                    isPaused = order.isPaused
                )
            },
            onCardClick = { },
            onPrimaryClick = { order ->
                if (order.trackingUrl != null) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(order.trackingUrl)))
                } else if (order.isOngoing) {
                    Toast.makeText(requireContext(), "Tracking info coming soon", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Buy again: ${order.name}", Toast.LENGTH_SHORT).show()
                }
            },
            onSecondaryClick = { }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}