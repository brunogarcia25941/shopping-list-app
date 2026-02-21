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
    val category: String = "Geral"
)

class ShoppingClient {
    // O IP Mágico (10.0.2.2 para emulador, localhost para PC/iOS Simulator)
    // para testar no telemóvel físico, pôr aqui o IP do PC (ex: 192.168.1.5)
    private val host = "10.0.2.2"
    private val port = 8080

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            })
        }
        // 2. Instalar o plugin de WebSockets no Cliente
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun getItems(): List<ShoppingItem> {
        return client.get("http://$host:$port/shopping-list").body()
    }

    suspend fun addItem(item: ShoppingItem) {
        client.post("http://$host:$port/shopping-list") {
            contentType(ContentType.Application.Json)
            setBody(item)
        }
    }

    suspend fun deleteItem(id: String) {
        client.delete("http://$host:$port/shopping-list/$id")
    }

    // Esta função devolve um "Flow" (um fluxo contínuo de dados)
    fun listenForUpdates(): Flow<String> = flow {
        try {
            // Conecta ao endpoint criado no servidor
            client.webSocket(method = HttpMethod.Get, host = host, port = port, path = "/shopping-list/updates") {
                // Fica num loop à espera de mensagens
                while (true) {
                    // Lemos o texto que o servidor mandou
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