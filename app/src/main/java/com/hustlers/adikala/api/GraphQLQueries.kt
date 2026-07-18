package com.hustlers.adikala.api

object GraphQLQueries {

    val GET_SHOP_INFO = """
        query {
            shop {
                name
                description
            }
        }
    """.trimIndent()

    fun getProducts(first: Int, after: String? = null) = """
        query {
            products(first: $first${if (after != null) ", after: \"$after\"" else ""}) {
                edges {
                    node {
                        id
                        title
                        handle
                        description
                        featuredImage { url altText }
                        images(first: 10) { edges { node { url altText } } }
                        priceRange { 
                            minVariantPrice { amount currencyCode }
                            maxVariantPrice { amount currencyCode }
                        }
                        variants(first: 20) {
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
                        productType
                        tags
                    }
                }
                pageInfo {
                    hasNextPage
                    endCursor
                }
            }
        }
    """.trimIndent()

    fun getProductByHandle(handle: String) = """
        query {
            productByHandle(handle: "$handle") {
                id
                title
                description
                handle
                productType
                tags
                featuredImage { url altText }
                images(first: 10) { edges { node { url altText } } }
                variants(first: 20) {
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
    """.trimIndent()

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
                            }
                        }
                    }
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

    fun getCollectionWithSort(handle: String, sortKey: String, first: Int = 50) = """
    query {
        collectionByHandle(handle: "$handle") {
            products(first: $first, sortKey: $sortKey) {
                edges {
                    node {
                        id
                        title
                        handle
                        description
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
                        collections(first: 3) {
                            edges {
                                node { title handle }
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
            products(first: $first, query: "$query") {
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
                        variants(first: 3) {
                            edges {
                                node {
                                    id
                                    title
                                    price { amount currencyCode }
                                    compareAtPrice { amount currencyCode }
                                    availableForSale
                                }
                            }
                        }
                        collections(first: 3) {
                            edges {
                                node { title handle }
                            }
                        }
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