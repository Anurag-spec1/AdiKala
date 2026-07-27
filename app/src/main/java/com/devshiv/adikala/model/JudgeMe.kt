package com.devshiv.adikala.model

data class JudgeMeReviewsResponse(
    val rating: Double?,
    val reviewNumber: Int?,
    val reviews: List<JudgeMeReviewData>?
)

data class JudgeMeReviewData(
    val id: Int?,
    val title: String?,
    val body: String?,
    val rating: Int?,
    val product_handle: String?,
    val reviewer: JudgeMeReviewer?,
    val created_at: String?,
    val verified_buyer: Boolean?,
    val pictures: List<JudgeMePicture>?
)

data class JudgeMeReviewer(
    val name: String?,
    val email: String?
)

data class JudgeMePicture(
    val url: String?,
    val urls: JudgeMePictureUrls?
)

data class JudgeMePictureUrls(
    val original: String?,
    val small: String?,
    val compact: String?,
    val huge: String?
)

data class JudgeMeProductResponse(
    val product: JudgeMeProduct?
)

data class JudgeMeProduct(
    val id: Int?,
    val external_id: String?,
    val handle: String?
)

data class JudgeMeSubmitReviewResponse(
    val id: Int?,
    val status: String?
)