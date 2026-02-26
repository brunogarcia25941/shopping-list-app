package com.example.shoppinglist



interface Platform {
    val name: String
}

expect fun getPlatform(): Platform