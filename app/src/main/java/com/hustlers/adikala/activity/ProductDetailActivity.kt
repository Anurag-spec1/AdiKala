package com.hustlers.adikala.activity

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.hustlers.adikala.R
import com.hustlers.adikala.adapter.ProductImageAdapter
import com.hustlers.adikala.adapter.ThumbnailAdapter
import com.hustlers.adikala.databinding.ActivityProductDetailBinding
import com.hustlers.adikala.dataclass.Product

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private lateinit var product: Product

    private lateinit var thumbAdapter: ThumbnailAdapter
    private var quantity = 1

    companion object {
        const val EXTRA_PRODUCT = "extra_product"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        product = (intent.getSerializableExtra(EXTRA_PRODUCT) as? Product) ?: run {
            finish(); return
        }

        bindProduct()
        setupGallery()
        setupQuantity()
        setupActions()
    }

    private fun bindProduct() {
        binding.tvTitle.text = product.name
        binding.tvBrand.text = product.brand
        binding.tvDescription.text = product.description
        binding.tvRating.text =
            if (product.rating > 0) "${product.rating} · No reviews yet" else "No reviews yet"

        binding.tvDiscountedPrice.text = "₹${product.discountedPrice}"
        binding.tvActualPrice.text = "₹${product.actualPrice}"
        binding.tvActualPrice.paintFlags =
            binding.tvActualPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        val onSale = product.discountedPrice < product.actualPrice
        if (onSale) {
            binding.tvSave.text = "Save ₹${product.actualPrice - product.discountedPrice}"
            binding.tvSave.visibility = View.VISIBLE
            binding.tvActualPrice.visibility = View.VISIBLE
        } else {
            binding.tvSave.visibility = View.GONE
            binding.tvActualPrice.visibility = View.GONE
        }

        binding.btnWishlist.setImageResource(
            if (product.isWishlisted) R.drawable.ic_heart_filled else R.drawable.ic_heart
        )
    }

    private fun setupGallery() {
        val images = if (product.images.isNotEmpty()) product.images else listOf(product.imageUrl)

        binding.vpImages.adapter = ProductImageAdapter(images)

        thumbAdapter = ThumbnailAdapter(images) { pos ->
            binding.vpImages.currentItem = pos   // tap thumb → swipe pager
        }
        binding.rvThumbs.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvThumbs.adapter = thumbAdapter

        // swipe pager → highlight thumb + update counter
        binding.tvPageCounter.text = "1 / ${images.size}"
        binding.vpImages.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    thumbAdapter.setSelected(position)
                    binding.rvThumbs.smoothScrollToPosition(position)
                    binding.tvPageCounter.text = "${position + 1} / ${images.size}"
                }
            }
        )

        // hide thumbs + counter when only one image
        val single = images.size <= 1
        binding.rvThumbs.visibility = if (single) View.GONE else View.VISIBLE
        binding.tvPageCounter.visibility = if (single) View.GONE else View.VISIBLE
    }

    private fun setupQuantity() {
        updateQty()
        binding.btnMinus.setOnClickListener {
            if (quantity > 1) { quantity--; updateQty() }
        }
        binding.btnPlus.setOnClickListener {
            if (quantity < 99) { quantity++; updateQty() }
        }
    }

    private fun updateQty() {
        binding.tvQty.text = quantity.toString()
        binding.btnMinus.alpha = if (quantity > 1) 1f else 0.35f
    }

    private fun setupActions() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnShare.setOnClickListener {
            val text = "${product.name}\n₹${product.discountedPrice} on Adikala"
            startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }, "Share via"))
        }

        binding.btnWishlist.setOnClickListener {
            product.isWishlisted = !product.isWishlisted
            binding.btnWishlist.setImageResource(
                if (product.isWishlisted) R.drawable.ic_heart_filled else R.drawable.ic_heart
            )
        }

        binding.btnAddToCart.setOnClickListener {
            Toast.makeText(this, "Added $quantity to cart", Toast.LENGTH_SHORT).show()
            // TODO: cart logic
        }

        binding.btnBuyNow.setOnClickListener {
            // TODO: checkout flow
        }
    }
}