package com.example.shoppinglist

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.* // <--- Importante para WebSockets
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.KotlinxSerializationConverter
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ShoppingItem(
    val id: String = "",
    val name: String,
    val quantity: Int,
    val isBought: Boolean = false,
    val category: String = "Geral",
    val familyCode: String = ""
)

class ShoppingClient(private val familyCode: String) {

    // 1. COLOCA AQUI O TEU LINK DO NGROK (Sem a barra / no fim)
    // Exemplo: "https://1234-abcd-5678.ngrok-free.app"
    private val baseUrl = "https://unpalatal-apocrine-shira.ngrok-free.dev"

    // 2. Prepara o link do WebSocket (Troca 'https' por 'wss')
    private val wsUrl = baseUrl.replace("https://", "wss://")

    private val client = HttpClient {
        install(ContentNegotiation) {
            json()
        }
        install(WebSockets)
    }

    suspend fun getItems(): List<ShoppingItem> {
        return client.get("$baseUrl/shopping-list/$familyCode").body()
    }

    suspend fun addItem(item: ShoppingItem) {
        client.post("$baseUrl/shopping-list/$familyCode") {
            contentType(ContentType.Application.Json)
            setBody(item)
        }
    }

    suspend fun updateItem(item: ShoppingItem) {
        client.put("$baseUrl/shopping-list/$familyCode/${item.id}") {
            contentType(ContentType.Application.Json)
            setBody(item)
        }
    }

    suspend fun deleteItem(id: String) {
        client.delete("$baseUrl/shopping-list/$familyCode/$id")
    }

    suspend fun clearBoughtItems() {
        client.delete("$baseUrl/shopping-list/$familyCode/bought")
    }

    // A chamada do WebSocket fica muito mais simples de escrever assim:
    fun listenForUpdates(): Flow<String> = flow {
        try {
            client.webSocket("$wsUrl/shopping-list/$familyCode/updates") {
                while (true) {
                    val message = (incoming.receive() as? io.ktor.websocket.Frame.Text)?.readText()
                    if (message != null) {
                        emit(message)
                    }
                }
            }
        } catch (e: Exception) {
            println("Erro no WebSocket: ${e.message}")
        }
    }
}