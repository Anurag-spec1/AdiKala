package com.devshiv.adikala.api

object GraphQLQueries {

    val GET_COLLECTIONS = """
    query {
        collections(first: 50) {
            edges {
                node {
                    id
                    title
                    handle
                    description
                    image { url altText }
                    products(first: 1) {
                        edges {
                            node {
                                id
                                featuredImage { url }
                                images(first: 1) { edges { node { url } } }
                            }
                        }
                    }
                }
            }
        }
    }
""".trimIndent()

    fun searchProducts(query: String, first: Int = 50) = """
    query {
        products(first: $first, query: "title:*$query* OR tag:*$query* OR product_type:*$query*") {
            edges {
                node {
                    id
                    title
                    handle
                    description
                    productType
                    tags
                    featuredImage { url altText }
                    images(first: 5) { edges { node { url altText } } }
                    priceRange { minVariantPrice { amount currencyCode } }
                    variants(first: 5) {
                        edges {
                            node {
                                id
                                title
                                price { amount currencyCode }
                                compareAtPrice { amount currencyCode }
                                availableForSale
                                selectedOptions { name value }
                                sku
                            }
                        }
                    }
                }
            }
        }
    }
""".trimIndent()

    fun getCollectionByHandleSorted(
        handle: String,
        sortKey: String,
        reverse: Boolean,
        first: Int = 250,
        after: String? = null
    ) = """
    query {
        collectionByHandle(handle: "$handle") {
            products(first: $first, sortKey: $sortKey, reverse: $reverse${if (after != null) ", after: \"$after\"" else ""}) {
                edges {
                    node {
                        id
                        title
                        handle
                        description
                        featuredImage { url altText }
                        images(first: 5) { edges { node { url altText } } }
                        priceRange { minVariantPrice { amount currencyCode } }
                        variants(first: 5) {
                            edges {
                                node {
                                    id
                                    title
                                    price { amount currencyCode }
                                    compareAtPrice { amount currencyCode }
                                    availableForSale
                                    selectedOptions { name value }
                                    sku
                                }
                            }
                        }
                        collections(first: 3) {
                            edges { node { title handle } }
                        }
                    }
                }
                pageInfo {
                    hasNextPage
                    endCursor
                }
            }
        }
    }
""".trimIndent()

    fun getCollectionByHandle(handle: String, first: Int = 250, after: String? = null) = """
        query {
            collectionByHandle(handle: "$handle") {
                id
                title
                description
                handle
                image { url altText }
                products(first: $first${if (after != null) ", after: \"$after\"" else ""}) {
                    edges {
                        node {
                            id
                            title
                            handle
                            description
                            productType
                            tags
                            featuredImage { url altText }
                            images(first: 5) { edges { node { url altText } } }
                            priceRange { 
                                minVariantPrice { amount currencyCode }
                            }
                            variants(first: 5) {
                                edges {
                                    node {
                                        id
                                        title
                                        price { amount currencyCode }
                                        compareAtPrice { amount currencyCode }
                                        availableForSale
                                        selectedOptions { name value }
                                        sku
                                    }
                                }
                            }
                            collections(first: 5) {
                                edges {
                                    node { title handle }
                                }
                            }
                        }
                    }
                    pageInfo {
                        hasNextPage
                        endCursor
                    }
                }
            }
        }
    """.trimIndent()

    val GET_MAIN_MENU = """
        query {
            menu(handle: "main-menu") {
                id
                title
                items {
                    id
                    title
                    url
                    type
                    resourceId
                    items {
                        id
                        title
                        url
                        type
                        resourceId
                    }
                }
            }
        }
    """.trimIndent()
}