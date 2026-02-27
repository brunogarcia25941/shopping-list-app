package com.brunogarcia.shoppinglist



interface Platform {
    val name: String
}

expect fun getPlatform(): Platform