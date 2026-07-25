package com.devshiv.adikala.model

enum class SortOption(val label: String) {
    FEATURED("Featured"),
    MOST_RELEVANT("Most Relevant"),
    BEST_SELLING("Best Selling"),
    NAME_ASC("Alphabetical: A–Z"),
    NAME_DESC("Alphabetical: Z–A"),
    PRICE_HIGH_LOW("Price: High to Low"),
    PRICE_LOW_HIGH("Price: Low to High"),
    DATE_NEW_OLD("Date: New to Old"),
    DATE_OLD_NEW("Date: Old to New")
}