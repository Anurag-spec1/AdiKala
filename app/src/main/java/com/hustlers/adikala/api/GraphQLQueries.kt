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

    val GET_PRODUCTS = """
        query {
            products(first: 50) {
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
                        }
                        variants(first: 10) {
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
                        products(first: 20) {
                            edges {
                                node {
                                    id
                                    title
                                    handle
                                    featuredImage { url altText }
                                    priceRange { 
                                        minVariantPrice { amount currencyCode }
                                    }
                                    variants(first: 3) {
                                        edges {
                                            node {
                                                id
                                                title
                                                price { amount currencyCode }
                                                availableForSale
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    """.trimIndent()

    fun getCollectionByHandle(handle: String) = """
        query {
            collectionByHandle(handle: "$handle") {
                id
                title
                description
                handle
                image { url altText }
                products(first: 50) {
                    edges {
                        node {
                            id
                            title
                            handle
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

    fun searchProducts(query: String) = """
        query {
            products(first: 30, query: "$query") {
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
                        variants(first: 3) {
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
    """.trimIndent()

    fun getProductRecommendations(productId: String) = """
        query {
            productRecommendations(productId: "gid://shopify/Product/$productId") {
                id
                title
                handle
                description
                featuredImage { url altText }
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
            }
        }
    """.trimIndent()

    val GET_MENU = """
        query {
            menu(handle: "main-menu") {
                id
                title
                items {
                    id
                    title
                    url
                    items {
                        id
                        title
                        url
                    }
                }
            }
        }
    """.trimIndent()
}