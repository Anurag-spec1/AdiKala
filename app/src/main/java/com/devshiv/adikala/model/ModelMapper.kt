package com.devshiv.adikala.model

object ModelMapper {

    fun mapToProduct(shopifyProduct: ShopifyProduct): Product {
        val variants = shopifyProduct.variants?.edges?.map { variantEdge ->
            val variant = variantEdge.node ?: return@map ProductVariant(
                id = "", title = "", price = 0.0, available = false
            )
            val options = mutableMapOf<String, String>()
            variant.selectedOptions?.forEach { option ->
                if (option.name != null && option.value != null) {
                    options[option.name] = option.value
                }
            }
            ProductVariant(
                id = variant.id?.removePrefix("gid://shopify/ProductVariant/") ?: "",
                title = variant.title ?: "",
                price = variant.price?.amount?.toDoubleOrNull() ?: 0.0,
                compareAtPrice = variant.compareAtPrice?.amount?.toDoubleOrNull(),
                available = variant.availableForSale ?: false,
                sku = variant.sku,
                options = options
            )
        } ?: emptyList()

        val images = shopifyProduct.images?.edges?.mapNotNull { it.node?.url } ?: emptyList()
        val imageUrl = shopifyProduct.featuredImage?.url ?: images.firstOrNull() ?: ""
        val firstVariant = variants.firstOrNull()

        val discountedPrice = firstVariant?.price ?: 0.0

        val createdAt = parseDate(shopifyProduct.createdAt)
        val actualPrice = firstVariant?.compareAtPrice ?: discountedPrice

        val collections = shopifyProduct.collections?.edges?.mapNotNull { it.node?.title } ?: emptyList()

        return Product(
            id = shopifyProduct.id?.removePrefix("gid://shopify/Product/") ?: "",
            name = shopifyProduct.title ?: "",
            handle = shopifyProduct.handle ?: "",
            description = shopifyProduct.description?.take(100)?.plus("...") ?: "",
            fullDescription = shopifyProduct.description ?: "",
            imageUrl = imageUrl,
            actualPrice = actualPrice.toInt(),
            discountedPrice = discountedPrice.toInt(),
            images = images,
            variants = variants,
            collections = collections,
            createdAt = createdAt,
            brand = "adikala.in"
        )
    }

    private fun parseDate(dateString: String?): Long {
        if (dateString.isNullOrEmpty()) return System.currentTimeMillis()

        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
            sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
            sdf.parse(dateString)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

}