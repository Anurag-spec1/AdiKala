package com.devshiv.adikala.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devshiv.adikala.databinding.ItemReviewBinding
import com.devshiv.adikala.model.Review
import java.text.SimpleDateFormat
import java.util.Locale

class ReviewAdapter(
    private val reviews: List<Review>
) : RecyclerView.Adapter<ReviewAdapter.VH>() {

    inner class VH(val b: ItemReviewBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(review: Review) {
            b.tvReviewerName.text = review.reviewerName
            b.tvReviewTitle.text = review.title.ifEmpty { "Review" }
            b.tvReviewBody.text = review.body
            b.tvReviewDate.text = formatDate(review.createdAt)

            if (review.isVerifiedBuyer) {
                b.tvVerified.visibility = View.VISIBLE
            } else {
                b.tvVerified.visibility = View.GONE
            }

            setupStars(review.rating)

            if (review.pictureUrls.isNotEmpty()) {
                b.ivReviewPhoto.visibility = View.VISIBLE
                Glide.with(b.ivReviewPhoto)
                    .load(review.pictureUrls.first())
                    .centerCrop()
                    .into(b.ivReviewPhoto)
            } else {
                b.ivReviewPhoto.visibility = View.GONE
            }
        }

        private fun setupStars(rating: Int) {
            val stars = listOf(b.star1, b.star2, b.star3, b.star4, b.star5)
            stars.forEachIndexed { index, star ->
                if (index < rating) {
                    star.setColorFilter(android.graphics.Color.parseColor("#F5A623"))
                } else {
                    star.setColorFilter(android.graphics.Color.parseColor("#E0E0E0"))
                }
            }
        }

        private fun formatDate(dateString: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                val outputFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
                val date = inputFormat.parse(dateString)
                date?.let { outputFormat.format(it) } ?: dateString
            } catch (e: Exception) {
                try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
                    val date = inputFormat.parse(dateString)
                    date?.let { outputFormat.format(it) } ?: dateString
                } catch (e2: Exception) {
                    dateString
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemReviewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(reviews[position])

    override fun getItemCount() = reviews.size
}