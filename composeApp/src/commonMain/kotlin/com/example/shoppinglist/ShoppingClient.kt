package com.example.shoppinglist

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ShoppingItem(
    val id: String = "",
    val name: String,
    val quantity: Int,
    val isBought: Boolean = false,
    val category: String = "Geral"
)

class ShoppingClient {
    // Configurar o cliente Ktor
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            })
        }
    }

    // O IP Mágico do Android Emulator
    private val host = "10.0.2.2"
    private val port = 8080

    // Função para buscar a lista
    suspend fun getItems(): List<ShoppingItem> {
        // Faz o pedido GET http://10.0.2.2:8080/shopping-list
        val response = client.get("http://$host:$port/shopping-list")
        return response.body()
    }

    // Função para adicionar item
    suspend fun addItem(item: ShoppingItem) {
        client.post("http://$host:$port/shopping-list") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(item)
        }
    }
}