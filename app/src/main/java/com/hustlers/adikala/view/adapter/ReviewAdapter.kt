package com.hustlers.adikala.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hustlers.adikala.databinding.ItemReviewBinding
import com.hustlers.adikala.model.Review

class ReviewAdapter(
    private val reviews: List<Review>
) : RecyclerView.Adapter<ReviewAdapter.VH>() {

    inner class VH(val b: ItemReviewBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(review: Review) {
            b.tvReviewerName.text = review.reviewerName
            b.tvReviewTitle.text = review.title.ifEmpty { "Review" }
            b.tvReviewBody.text = review.body
            b.tvReviewDate.text = review.createdAt

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
                star.isSelected = index < rating
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