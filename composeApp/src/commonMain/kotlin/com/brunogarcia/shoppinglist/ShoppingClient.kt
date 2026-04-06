package com.brunogarcia.shoppinglist

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.* // <--- Importante para WebSockets
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.serialization.kotlinx.KotlinxSerializationConverter
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds




class ShoppingClient(private val familyCode: String) {

    private val baseUrl = "https://shopping-backend-familia.onrender.com"

    // Preparar o link do WebSocket (Troca 'https' por 'wss')
    private val wsUrl = baseUrl.replace("https://", "wss://")

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
        install(WebSockets){
            pingInterval = 20.seconds // Manda um "Estou vivo" a cada 20 segundos
        }
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

    // Vai buscar apenas um item específico (com a foto inteira)
    suspend fun getItem(id: String): ShoppingItem {
        return client.get("$baseUrl/shopping-list/$familyCode/$id").body()
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


    suspend fun getSuggestions(): List<QuickSuggestion> {
        return client.get("$baseUrl/shopping-list/$familyCode/suggestions").body()
    }

    suspend fun addSuggestion(name: String): QuickSuggestion {
        return client.post("$baseUrl/shopping-list/$familyCode/suggestions") {
            contentType(ContentType.Application.Json)
            setBody(QuickSuggestion(name = name))
        }.body()
    }

    suspend fun deleteSuggestion(id: String) {
        client.delete("$baseUrl/shopping-list/$familyCode/suggestions/$id")
    }

    // Verifica qual a versão mínima exigida pelo servidor
    suspend fun checkMinimumVersion(): Int {
        return try {
            val response: HttpResponse = client.get("$baseUrl/api/config")

            // O servidor devolve {"minVersion": 1}. Lemos apenas esse número.
            val json = response.bodyAsText()
            val regex = """"minVersion"\s*:\s*(\d+)""".toRegex()
            val matchResult = regex.find(json)
            matchResult?.groupValues?.get(1)?.toInt() ?: 1
        } catch (e: Exception) {
            // Se falhar a net ou o servidor estiver a dormir, deixamos a pessoa usar a app na mesma (retorna 1)
            1
        }
    }
}

