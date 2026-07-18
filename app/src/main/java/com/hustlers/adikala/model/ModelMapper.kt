package com.hustlers.adikala.model

import java.text.NumberFormat
import java.util.Locale

object ModelMapper {

    fun mapToProduct(shopifyProduct: ShopifyProduct): Product {
        val variants = shopifyProduct.variants?.edges?.map { variantEdge ->
            val variant = variantEdge.node ?: return@map ProductVariant(
                id = "",
                title = "",
                price = 0.0,
                available = false
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
        val price = firstVariant?.price ?: 0.0
        val compareAtPrice = firstVariant?.compareAtPrice

        return Product(
            id = shopifyProduct.id?.removePrefix("gid://shopify/Product/") ?: "",
            name = shopifyProduct.title ?: "",
            handle = shopifyProduct.handle ?: "",
            description = shopifyProduct.description?.take(100)?.plus("...") ?: "",
            fullDescription = shopifyProduct.description ?: "",
            imageUrl = imageUrl,
            actualPrice = price.toInt(),
            discountedPrice = compareAtPrice?.toInt() ?: price.toInt(),
            images = images,
            variants = variants,
            collections = shopifyProduct.collections?.edges?.mapNotNull { it.node?.title } ?: emptyList()
        )
    }

    fun mapToCategory(shopifyCollection: ShopifyCollection, index: Int): Category {
        val subCategories = shopifyCollection.products?.edges?.mapNotNull { productEdge ->
            val product = productEdge.node
            if (product?.collections?.edges?.isNotEmpty() == true) {
                val subCollection = product.collections.edges.firstOrNull()?.node
                SubCategory(
                    id = subCollection?.id?.hashCode() ?: 0,
                    name = subCollection?.title ?: "",
                    handle = subCollection?.handle ?: "",
                    image = subCollection?.image?.url ?: "",
                    itemCount = subCollection?.products?.edges?.size ?: 0
                )
            } else {
                null
            }
        }?.distinctBy { it.name }?.take(10) ?: emptyList()

        return Category(
            id = shopifyCollection.id?.hashCode() ?: index,
            name = shopifyCollection.title ?: "",
            handle = shopifyCollection.handle ?: "",
            image = shopifyCollection.image?.url ?: "",
            itemCount = shopifyCollection.products?.edges?.size ?: 0,
            subCategories = subCategories
        )
    }

    fun mapToPopularCollection(shopifyCollection: ShopifyCollection, index: Int): PopularCollection {
        return PopularCollection(
            id = shopifyCollection.id?.hashCode() ?: index,
            name = shopifyCollection.title ?: "",
            handle = shopifyCollection.handle ?: "",
            image = shopifyCollection.image?.url ?: ""
        )
    }

    fun extractNumericId(gid: String): String {
        return gid.substringAfterLast("/").split("?").first()
    }
}