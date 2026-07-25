package com.devshiv.adikala.view.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.devshiv.adikala.view.adapter.OrderAdapter
import com.devshiv.adikala.databinding.FragmentOrdersBinding
import com.devshiv.adikala.model.Order
import com.devshiv.adikala.model.OrderStatus

class OrdersFragment : Fragment() {

    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!

    private val allOrders = mutableListOf<Order>()
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

        binding.rvOrders.layoutManager = LinearLayoutManager(requireContext())

        loadOrders()
        setupTabs()
        render()
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
        binding.btnHelp.setOnClickListener { }
        binding.btnShopNow.setOnClickListener { }
        refreshCounts()
        updateTabStyles()
    }

    private fun refreshCounts() {
        val ongoing = allOrders.count { it.isOngoing }
        val past = allOrders.count { !it.isOngoing }
        binding.tvCountOngoing.text = ongoing.toString()
        binding.tvCountPast.text = past.toString()
    }

    private fun updateTabStyles() {
        val teal = Color.parseColor("#0D3238")
        val transparent = Color.TRANSPARENT
        val white = Color.WHITE
        val muted = Color.parseColor("#8A8A8A")
        val badgeOnTeal = Color.parseColor("#33FFFFFF")
        val badgeOnLight = Color.parseColor("#14000000")

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
            binding.tabOngoing.elevation=0f
            binding.tvTabOngoing.setTextColor(muted)
            binding.tvCountOngoing.setBackgroundColor(badgeOnLight)
            binding.tvCountOngoing.setTextColor(muted)
        }
    }

    private fun render() {
        val filtered =
            if (showingOngoing) allOrders.filter { it.isOngoing }
            else allOrders.filter { !it.isOngoing }

        if (filtered.isEmpty()) {
            binding.rvOrders.visibility = View.GONE
            binding.emptyState.visibility = View.VISIBLE
            binding.tvEmptyTitle.text =
                if (showingOngoing) "No ongoing orders" else "No past orders"
            binding.tvEmptySubtitle.text =
                if (showingOngoing) "Your active orders will show up here"
                else "Delivered and closed orders appear here"
            return
        }

        binding.rvOrders.visibility = View.VISIBLE
        binding.emptyState.visibility = View.GONE

        binding.rvOrders.adapter = OrderAdapter(
            filtered,
            onCardClick = { },
            onPrimaryClick = { order ->
                val msg = if (order.isOngoing) "Tracking ${order.orderId}" else "Reordering ${order.orderId}"
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            },
            onSecondaryClick = { }
        )
    }

    private fun loadOrders() {
        allOrders.clear()
        allOrders.addAll(
            listOf(
                Order(
                    "ADK10482",
                    "Peacock printed MDF Cutout Pack of 4",
                    "https://www.adikala.in/cdn/shop/collections/pichwai.jpg?v=1737890053&width=1080",
                    2, 698, "12 Jul 2026", OrderStatus.SHIPPED, "18 Jul"
                ),
                Order(
                    "ADK10461",
                    "Decorative Wooden Beads Assorted",
                    "https://www.adikala.in/cdn/shop/products/657_cdb4596c-0ceb-47dc-9652-09be375b15c0.jpg?v=1657903140&width=1080",
                    1, 349, "10 Jul 2026", OrderStatus.OUT_FOR_DELIVERY, "14 Jul"
                ),
                Order(
                    "ADK10399",
                    "MDF Base Round Pack of 6",
                    "https://www.adikala.in/cdn/shop/collections/for-DIY.jpg?v=1674845390&width=1080",
                    3, 1247, "28 Jun 2026", OrderStatus.DELIVERED,
                    deliveredOn = "3 Jul 2026"
                ),
                Order(
                    "ADK10344",
                    "Decorative Essentials Craft Kit",
                    "https://www.adikala.in/cdn/shop/collections/decoration.jpg?v=1674844832&width=1080",
                    1, 899, "15 Jun 2026", OrderStatus.CANCELLED
                )
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}