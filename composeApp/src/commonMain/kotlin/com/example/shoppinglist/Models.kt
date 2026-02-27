package com.example.shoppinglist


import kotlinx.serialization.Serializable

@Serializable
data class ShoppingItem(
    val id: String = "",
    val name: String,
    val quantity: Int,
    val isBought: Boolean = false,
    val category: String = "Geral",
    val familyCode: String = "",
    val notes: String? = null,
    val photoBase64: String? = null
)

@Serializable
data class WsMessage(
    val action: String,
    val item: ShoppingItem? = null,
    val itemId: String? = null
)

@Serializable
data class QuickSuggestion(
    val id: String = "",
    val familyCode: String = "",
    val name: String
)