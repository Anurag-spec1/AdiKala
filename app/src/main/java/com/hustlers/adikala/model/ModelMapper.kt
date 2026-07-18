package com.hustlers.adikala.model

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
        val collections = shopifyProduct.collections?.edges?.mapNotNull { it.node?.title } ?: emptyList()

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
            collections = collections,
            brand = "adikala.in"
        )
    }

    fun mapToCategory(shopifyCollection: ShopifyCollection, index: Int): Category {
        return Category(
            id = index,
            name = shopifyCollection.title ?: "",
            handle = shopifyCollection.handle ?: "",
            image = shopifyCollection.image?.url ?: "",
            itemCount = shopifyCollection.products?.edges?.size ?: 0
        )
    }

    fun mapToSubCategories(products: List<Product>, collectionTitle: String): List<SubCategory> {
        return products
            .flatMap { it.collections }
            .filter { it != collectionTitle }
            .distinct()
            .map { collectionName ->
                val matchingProducts = products.filter { it.collections.contains(collectionName) }
                SubCategory(
                    id = collectionName.hashCode(),
                    name = collectionName,
                    image = matchingProducts.firstOrNull()?.imageUrl ?: "",
                    itemCount = matchingProducts.size,
                    handle = collectionName.lowercase().replace(" ", "-")
                )
            }
            .filter { it.itemCount > 0 }
            .take(15)
    }
}