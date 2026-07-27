package com.devshiv.adikala.api

object CustomerQueries {

    val GET_PROFILE = """
        query Profile {
            customer {
                id
                firstName
                lastName
                emailAddress { emailAddress }
                phoneNumber { phoneNumber }
                defaultAddress {
                    address1
                    address2
                    city
                    country
                    zip
                    phoneNumber
                }
            }
        }
    """.trimIndent()

    val GET_ORDERS = """
        query Orders {
            customer {
                orders(first: 50) {
                    edges {
                        node {
                            id
                            number
                            name
                            processedAt
                            fulfillmentStatus
                            financialStatus
                            totalPrice { amount currencyCode }
                            lineItems(first: 20) {
                                edges {
                                    node {
                                        title
                                        quantity
                                        variant {
                                            title
                                            price { amount }
                                            image { url }
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

    val GET_ADDRESSES = """
        query Addresses {
            customer {
                addresses(first: 20) {
                    edges {
                        node {
                            id
                            address1
                            address2
                            city
                            country
                            zip
                            phoneNumber
                            firstName
                            lastName
                        }
                    }
                }
                defaultAddress {
                    id
                    address1
                    city
                    country
                    zip
                }
            }
        }
    """.trimIndent()
}