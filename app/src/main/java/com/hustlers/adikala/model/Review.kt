package com.hustlers.adikala.model

data class Review(
    val id: Long = 0,
    val rating: Int = 0,
    val title: String = "",
    val body: String = "",
    val reviewerName: String = "",
    val reviewerEmail: String = "",
    val createdAt: String = "",
    val pictureUrls: List<String> = emptyList(),
    val isVerifiedBuyer: Boolean = false
)

data class RatingSummary(
    val averageRating: Double = 0.0,
    val totalReviews: Int = 0,
    val distribution: Map<Int, Int> = emptyMap()
)