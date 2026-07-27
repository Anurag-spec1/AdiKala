package com.devshiv.adikala.view.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.devshiv.adikala.R
import com.devshiv.adikala.view.adapter.ReviewAdapter
import com.devshiv.adikala.view.adapter.ThumbnailAdapter
import com.devshiv.adikala.databinding.ActivityProductDetailBinding
import com.devshiv.adikala.databinding.ItemDiscountBinding
import com.devshiv.adikala.databinding.ItemFeatureBinding
import com.devshiv.adikala.model.Product
import com.devshiv.adikala.viewmodel.CartViewModel
import com.devshiv.adikala.viewmodel.ProductDetailViewModel

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private lateinit var viewModel: ProductDetailViewModel
    private lateinit var cartViewModel: CartViewModel
    private lateinit var product: Product
    private lateinit var thumbAdapter: ThumbnailAdapter
    private lateinit var reviewAdapter: ReviewAdapter

    private lateinit var images: List<String>
    private var quantity = 1
    private var userRating = 0
    private var selectedVariantId: String? = null

    companion object {
        const val EXTRA_PRODUCT = "extra_product"
        private const val SUPPORT_PHONE = "+918130507350"
        private const val SUPPORT_WA = "918130507350"
    }

    private data class Offer(val code: String, val percent: String, val eligibility: String)

    private val offers = listOf(
        Offer("ADIF3", "3% off", "First-time buyer · ₹500–₹1,999"),
        Offer("ADIF05", "5% off", "First-time buyer · ₹2,000–₹4,999"),
        Offer("ADIF7", "7% off", "All buyers · ₹5,000–₹9,999"),
        Offer("ADIR12", "12% off", "All buyers · ₹10,000–₹49,999"),
        Offer("ADIB15", "15% off", "All buyers · ₹50,000 & above")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ProductDetailViewModel::class.java]
        cartViewModel = ViewModelProvider(this)[CartViewModel::class.java]

        product = (intent.getSerializableExtra(EXTRA_PRODUCT) as? Product) ?: run { finish(); return }

        // Set default variant
        selectedVariantId = product.variants.firstOrNull()?.id

        viewModel.loadProduct(product)

        bindProduct()
        setupGallery()
        setupFeatures()
        setupOffers()
        setupSections()
        setupVariants()
        setupQuantity()
        setupActions()
        setupReviews()
        observeViewModel()
    }

    private fun bindProduct() {
        binding.tvTitle.text = product.name
        binding.tvBrand.text = product.brand
        binding.tvDescription.text =
            if (product.fullDescription.isNotBlank()) product.fullDescription else product.description

        binding.tvDiscountedPrice.text = "₹${product.discountedPrice}"
        binding.tvActualPrice.text = "₹${product.actualPrice}"
        binding.tvActualPrice.paintFlags = binding.tvActualPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        val onSale = product.discountedPrice < product.actualPrice
        binding.tvSave.text = "Save ₹${product.actualPrice - product.discountedPrice}"
        binding.tvSave.visibility = if (onSale) View.VISIBLE else View.GONE
        binding.tvActualPrice.visibility = if (onSale) View.VISIBLE else View.GONE

        binding.tvRatingBadge.text = if (product.rating > 0) "${product.rating}" else "New"

        binding.btnWishlist.setImageResource(
            if (product.isWishlisted) R.drawable.ic_heart_filled else R.drawable.ic_heart
        )
    }

    private fun setupGallery() {
        images = if (product.images.isNotEmpty()) product.images else listOf(product.imageUrl)
        showMainImage(0)

        thumbAdapter = ThumbnailAdapter(images) { pos ->
            thumbAdapter.setSelected(pos)
            showMainImage(pos)
        }
        binding.rvThumbs.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvThumbs.adapter = thumbAdapter
        binding.rvThumbs.visibility = if (images.size <= 1) View.GONE else View.VISIBLE
    }

    private fun showMainImage(pos: Int) {
        Glide.with(binding.imgMain)
            .load(images[pos])
            .transition(DrawableTransitionOptions.withCrossFade(220))
            .centerCrop()
            .into(binding.imgMain)
    }

    private fun setupFeatures() {
        val container = binding.featureContainer
        container.removeAllViews()
        if (product.features.isEmpty()) { container.visibility = View.GONE; return }
        product.features.forEach { feature ->
            val row = ItemFeatureBinding.inflate(layoutInflater, container, false)
            row.tvFeature.text = feature
            container.addView(row.root)
        }
    }

    private fun setupOffers() {
        val container = binding.discountContainer
        container.removeAllViews()
        offers.forEach { offer ->
            val row = ItemDiscountBinding.inflate(layoutInflater, container, false)
            row.tvCode.text = offer.code
            row.tvPercent.text = offer.percent
            row.tvEligibility.text = offer.eligibility
            row.rowDiscount.setOnClickListener { copyCode(offer.code) }
            container.addView(row.root)
        }
    }

    private fun copyCode(code: String) {
        val cb = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        cb.setPrimaryClip(ClipData.newPlainText("code", code))
        Toast.makeText(this, "$code copied", Toast.LENGTH_SHORT).show()
    }

    private fun setupSections() {
        binding.chevDesc.rotation = 180f
        toggleSection(binding.headerDesc, binding.contentDesc, binding.chevDesc)
        toggleSection(binding.headerShip, binding.contentShip, binding.chevShip)
        toggleSection(binding.headerOffers, binding.contentOffers, binding.chevOffers)
        toggleSection(binding.headerBulk, binding.contentBulk, binding.chevBulk)
        toggleSection(binding.headerReviews, binding.contentReviews, binding.chevReviews)
        toggleSection(binding.headerAsk, binding.contentAsk, binding.chevAsk)
    }

    private fun toggleSection(header: View, content: View, chevron: View) {
        header.setOnClickListener {
            val expanding = content.visibility != View.VISIBLE
            TransitionManager.beginDelayedTransition(binding.scrollContent, AutoTransition().setDuration(200))
            content.visibility = if (expanding) View.VISIBLE else View.GONE
            chevron.animate().rotation(if (expanding) 180f else 0f).setDuration(200).start()
        }
    }

    private fun setupVariants() {
        val variants = product.variants
        if (variants.isEmpty() || variants.size <= 1 && variants.firstOrNull()?.title == "Default Title") {
            binding.variantContainer.visibility = View.GONE
            return
        }

        binding.variantContainer.visibility = View.VISIBLE
        binding.variantContainer.removeAllViews()

        variants.forEach { variant ->
            val chip = com.google.android.material.chip.Chip(this).apply {
                text = variant.title
                isCheckable = true
                isChecked = variant.id == selectedVariantId
                setOnClickListener {
                    selectedVariantId = variant.id
                    // Update all chips
                    for (i in 0 until binding.variantContainer.childCount) {
                        (binding.variantContainer.getChildAt(i) as? com.google.android.material.chip.Chip)?.isChecked = false
                    }
                    isChecked = true
                }
            }
            binding.variantContainer.addView(chip)
        }
    }

    private fun setupQuantity() {
        updateQty()
        binding.btnMinus.setOnClickListener { if (quantity > 1) { quantity--; updateQty() } }
        binding.btnPlus.setOnClickListener { if (quantity < 99) { quantity++; updateQty() } }
    }

    private fun updateQty() {
        binding.tvQty.text = quantity.toString()
        binding.btnMinus.alpha = if (quantity > 1) 1f else 0.35f
    }

    private fun getVariantId(): String {
        return selectedVariantId?.let { id ->
            if (id.startsWith("gid://")) id else "gid://shopify/ProductVariant/$id"
        } ?: "gid://shopify/ProductVariant/${product.variants.firstOrNull()?.id ?: product.id}"
    }

    private fun setupActions() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnShare.setOnClickListener {
            val text = "${product.name}\n₹${product.discountedPrice} on Adikala"
            startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text)
            }, "Share via"))
        }

        binding.btnWishlist.setOnClickListener {
            product.isWishlisted = !product.isWishlisted
            binding.btnWishlist.setImageResource(
                if (product.isWishlisted) R.drawable.ic_heart_filled else R.drawable.ic_heart
            )
        }

        binding.btnCall.setOnClickListener {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$SUPPORT_PHONE")))
        }

        binding.btnWhatsapp.setOnClickListener {
            val msg = Uri.encode("Hi, I'm interested in bulk/custom order for ${product.name}")
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$SUPPORT_WA?text=$msg")))
            } catch (e: Exception) {
                Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnAddToCart.setOnClickListener {
            cartViewModel.addItem(getVariantId(), product.name, quantity)
            Toast.makeText(this, "Added $quantity to cart", Toast.LENGTH_SHORT).show()
        }

        binding.btnBuyNow.setOnClickListener {
            cartViewModel.addItem(getVariantId(), product.name, quantity)
            startActivity(Intent(this, CartActivity::class.java))
        }

        binding.btnSubmitQuery.setOnClickListener { submitQuery() }
    }

    private fun setupReviews() {
        reviewAdapter = ReviewAdapter(emptyList())
        binding.rvReviews.layoutManager = LinearLayoutManager(this)
        binding.rvReviews.adapter = reviewAdapter
        binding.rvReviews.isNestedScrollingEnabled = false

        binding.btnWriteReview.setOnClickListener {
            binding.reviewForm.visibility = View.VISIBLE
            binding.btnWriteReview.visibility = View.GONE
        }

        binding.btnCancelReview.setOnClickListener {
            binding.reviewForm.visibility = View.GONE
            binding.btnWriteReview.visibility = View.VISIBLE
            clearReviewForm()
        }

        val starButtons = listOf(binding.reviewStar1, binding.reviewStar2, binding.reviewStar3, binding.reviewStar4, binding.reviewStar5)
        starButtons.forEachIndexed { index, star ->
            star.setOnClickListener {
                userRating = index + 1
                updateStarSelection(index + 1)
            }
        }

        binding.btnSubmitReview.setOnClickListener { submitReview() }
    }

    private fun updateStarSelection(rating: Int) {
        val stars = listOf(binding.reviewStar1, binding.reviewStar2, binding.reviewStar3, binding.reviewStar4, binding.reviewStar5)
        stars.forEachIndexed { index, star ->
            star.isSelected = index < rating
        }
    }

    private fun clearReviewForm() {
        userRating = 0
        binding.etReviewTitle.text?.clear()
        binding.etReviewBody.text?.clear()
        binding.etReviewerName.text?.clear()
        binding.etReviewerEmail.text?.clear()
        updateStarSelection(0)
    }

    private fun submitReview() {
        val title = binding.etReviewTitle.text.toString().trim()
        val body = binding.etReviewBody.text.toString().trim()
        val name = binding.etReviewerName.text.toString().trim()
        val email = binding.etReviewerEmail.text.toString().trim()

        if (userRating == 0) { Toast.makeText(this, "Select a rating", Toast.LENGTH_SHORT).show(); return }
        if (name.isEmpty()) { binding.etReviewerName.error = "Enter your name"; return }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etReviewerEmail.error = "Enter valid email"; return
        }

        viewModel.submitReview(userRating, title.ifEmpty { null }, body.ifEmpty { null }, name, email)
        clearReviewForm()
        binding.reviewForm.visibility = View.GONE
        binding.btnWriteReview.visibility = View.VISIBLE
    }

    private fun observeViewModel() {
        viewModel.ratingSummary.observe(this) { summary ->
            binding.tvRating.text = String.format("%.1f", summary.averageRating)
            binding.tvRatingBadge.text = String.format("%.1f", summary.averageRating)
            binding.tvReviewCount.text = "Based on ${summary.totalReviews} reviews"

            binding.tvRatingBar5Count.text = "${summary.distribution[5] ?: 0}"
            binding.tvRatingBar4Count.text = "${summary.distribution[4] ?: 0}"
            binding.tvRatingBar3Count.text = "${summary.distribution[3] ?: 0}"
            binding.tvRatingBar2Count.text = "${summary.distribution[2] ?: 0}"
            binding.tvRatingBar1Count.text = "${summary.distribution[1] ?: 0}"

            val total = (summary.totalReviews).coerceAtLeast(1)
            binding.progressRating5.progress = ((summary.distribution[5] ?: 0) * 100) / total
            binding.progressRating4.progress = ((summary.distribution[4] ?: 0) * 100) / total
            binding.progressRating3.progress = ((summary.distribution[3] ?: 0) * 100) / total
            binding.progressRating2.progress = ((summary.distribution[2] ?: 0) * 100) / total
            binding.progressRating1.progress = ((summary.distribution[1] ?: 0) * 100) / total
        }

        viewModel.reviews.observe(this) { reviews ->
            Log.d("ProductDetail", "Got ${reviews.size} reviews for current product")
            reviewAdapter = ReviewAdapter(reviews)
            binding.rvReviews.adapter = reviewAdapter
        }

        viewModel.isLoadingReviews.observe(this) { isLoading ->
            binding.shimmerReviews.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.isSubmitting.observe(this) { isSubmitting ->
            binding.btnSubmitReview.isEnabled = !isSubmitting
        }

        viewModel.submitResult.observe(this) { result ->
            if (result.isNotEmpty()) Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
        }

        cartViewModel.itemAdded.observe(this) { added ->
            if (added == true) cartViewModel.clearItemAdded()
        }
    }

    private fun submitQuery() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val msg = binding.etMessage.text.toString().trim()

        if (name.isEmpty()) { binding.etName.error = "Enter your name"; return }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Enter a valid email"; return
        }
        if (msg.isEmpty()) { binding.etMessage.error = "Enter your message"; return }

        Toast.makeText(this, "Thanks! We'll get back to you soon.", Toast.LENGTH_LONG).show()
        binding.etName.text?.clear()
        binding.etEmail.text?.clear()
        binding.etPhone.text?.clear()
        binding.etMessage.text?.clear()
    }
}