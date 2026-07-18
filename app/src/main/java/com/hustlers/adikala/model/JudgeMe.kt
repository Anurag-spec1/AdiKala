package com.hustlers.adikala.model

data class JudgeMeReviewsResponse(
    val rating: Double? = null,
    val reviewNumber: Int? = null,
    val reviews: List<JudgeMeReviewData>? = null,
    val ratingDistribution: List<List<Any>>? = null,
    val curPage: Int? = null,
    val totalPage: Int? = null
)

data class JudgeMeReviewData(
    val id: Long? = null,
    val rating: Int? = null,
    val title: String? = null,
    val body: String? = null,
    val reviewer_name: String? = null,
    val reviewer_email: String? = null,
    val created_at: String? = null,
    val pictures: List<JudgeMePicture>? = null,
    val verified_buyer: Boolean? = null,
    val featured: Boolean? = null
)

data class JudgeMePicture(
    val url: String? = null,
    val thumb_url: String? = null
)

data class JudgeMeSubmitReviewResponse(
    val id: Long? = null,
    val status: String? = null,
    val message: String? = null,
    val error: String? = null
)