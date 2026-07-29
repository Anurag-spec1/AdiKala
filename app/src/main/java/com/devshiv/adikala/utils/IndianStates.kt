package com.devshiv.adikala.utils

object IndianStates {

    data class StateEntry(val name: String, val code: String)

    val all: List<StateEntry> = listOf(
        StateEntry("Andhra Pradesh", "AP"),
        StateEntry("Arunachal Pradesh", "AR"),
        StateEntry("Assam", "AS"),
        StateEntry("Bihar", "BR"),
        StateEntry("Chhattisgarh", "CG"),
        StateEntry("Goa", "GA"),
        StateEntry("Gujarat", "GJ"),
        StateEntry("Haryana", "HR"),
        StateEntry("Himachal Pradesh", "HP"),
        StateEntry("Jharkhand", "JH"),
        StateEntry("Karnataka", "KA"),
        StateEntry("Kerala", "KL"),
        StateEntry("Madhya Pradesh", "MP"),
        StateEntry("Maharashtra", "MH"),
        StateEntry("Manipur", "MN"),
        StateEntry("Meghalaya", "ML"),
        StateEntry("Mizoram", "MZ"),
        StateEntry("Nagaland", "NL"),
        StateEntry("Odisha", "OD"),
        StateEntry("Punjab", "PB"),
        StateEntry("Rajasthan", "RJ"),
        StateEntry("Sikkim", "SK"),
        StateEntry("Tamil Nadu", "TN"),
        StateEntry("Telangana", "TS"),
        StateEntry("Tripura", "TR"),
        StateEntry("Uttar Pradesh", "UP"),
        StateEntry("Uttarakhand", "UK"),
        StateEntry("West Bengal", "WB"),
        StateEntry("Andaman and Nicobar Islands", "AN"),
        StateEntry("Chandigarh", "CH"),
        StateEntry("Dadra and Nagar Haveli and Daman and Diu", "DN"),
        StateEntry("Delhi", "DL"),
        StateEntry("Jammu and Kashmir", "JK"),
        StateEntry("Ladakh", "LA"),
        StateEntry("Lakshadweep", "LD"),
        StateEntry("Puducherry", "PY")
    )

    val names: List<String> = all.map { it.name }

     fun codeForName(name: String): String? =
        all.firstOrNull { it.name.equals(name.trim(), ignoreCase = true) }?.code

    fun nameForCode(code: String): String? =
        all.firstOrNull { it.code.equals(code.trim(), ignoreCase = true) }?.name
}