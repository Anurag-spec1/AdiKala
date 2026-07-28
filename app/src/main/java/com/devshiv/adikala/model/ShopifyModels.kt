package com.devshiv.adikala.model

import com.google.gson.annotations.SerializedName

data class GraphQLRequest(
    @SerializedName("query")
    val query: String
)

data class GraphQLResponse(
    @SerializedName("data")
    val data: ShopifyData? = null
)
data class ShopifyData(
    @SerializedName("shop") val shop: ShopifyShop? = null,
    @SerializedName("products") val products: ShopifyProducts? = null,
    @SerializedName("productByHandle") val productByHandle: ShopifyProduct? = null,
    @SerializedName("collections") val collections: ShopifyCollections? = null,
    @SerializedName("collectionByHandle") val collectionByHandle: ShopifyCollection? = null,
    @SerializedName("menu") val menu: ShopifyMenu? = null
)

data class ShopifyShop(
    @SerializedName("name") val name: String? = null,
    @SerializedName("description") val description: String? = null
)

data class ShopifyProducts(
    @SerializedName("edges") val edges: List<ShopifyProductEdge>? = null,
    @SerializedName("pageInfo") val pageInfo: ShopifyPageInfo? = null
)

data class ShopifyPageInfo(
    @SerializedName("hasNextPage") val hasNextPage: Boolean? = null,
    @SerializedName("endCursor") val endCursor: String? = null
)

data class ShopifyProductEdge(
    @SerializedName("node") val node: ShopifyProduct? = null
)

data class ShopifyProduct(
    @SerializedName("id") val id: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("handle") val handle: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("productType") val productType: String? = null,
    @SerializedName("tags") val tags: List<String>? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("featuredImage") val featuredImage: ShopifyImage? = null,
    @SerializedName("images") val images: ShopifyImages? = null,
    @SerializedName("priceRange") val priceRange: ShopifyPriceRange? = null,
    @SerializedName("variants") val variants: ShopifyVariants? = null,
    @SerializedName("collections") val collections: ShopifyCollections? = null
)

data class ShopifyImage(
    @SerializedName("url") val url: String? = null,
    @SerializedName("altText") val altText: String? = null
)

data class ShopifyImages(
    @SerializedName("edges") val edges: List<ShopifyImageEdge>? = null
)

data class ShopifyImageEdge(
    @SerializedName("node") val node: ShopifyImage? = null
)

data class ShopifyPriceRange(
    @SerializedName("minVariantPrice") val minVariantPrice: ShopifyPrice? = null,
    @SerializedName("maxVariantPrice") val maxVariantPrice: ShopifyPrice? = null
)

data class ShopifyPrice(
    @SerializedName("amount") val amount: String? = null,
    @SerializedName("currencyCode") val currencyCode: String? = null
)

data class ShopifyVariants(
    @SerializedName("edges") val edges: List<ShopifyVariantEdge>? = null
)

data class ShopifyVariantEdge(
    @SerializedName("node") val node: ShopifyVariant? = null
)

data class ShopifyVariant(
    @SerializedName("id") val id: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("price") val price: ShopifyPrice? = null,
    @SerializedName("compareAtPrice") val compareAtPrice: ShopifyPrice? = null,
    @SerializedName("availableForSale") val availableForSale: Boolean? = null,
    @SerializedName("selectedOptions") val selectedOptions: List<ShopifySelectedOption>? = null,
    @SerializedName("sku") val sku: String? = null
)

data class ShopifySelectedOption(
    @SerializedName("name") val name: String? = null,
    @SerializedName("value") val value: String? = null
)

data class ShopifyCollections(
    @SerializedName("edges") val edges: List<ShopifyCollectionEdge>? = null
)

data class ShopifyCollectionEdge(
    @SerializedName("node") val node: ShopifyCollection? = null
)

data class ShopifyCollection(
    @SerializedName("id") val id: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("handle") val handle: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("image") val image: ShopifyImage? = null,
    @SerializedName("products") val products: ShopifyProducts? = null
)

data class ShopifyMenu(
    @SerializedName("id") val id: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("items") val items: List<ShopifyMenuItem>? = null
)

data class ShopifyMenuItem(
    @SerializedName("id") val id: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("resourceId") val resourceId: String? = null,
    @SerializedName("items") val items: List<ShopifyMenuItem>? = null
)