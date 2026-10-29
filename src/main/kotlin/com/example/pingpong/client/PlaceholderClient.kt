package com.example.pingpong.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Company(val name: String)

@Serializable
data class User(
    val id: Int,
    @SerialName("username") val displayName: String,
    val email: String,
    val company: Company,
)

@Serializable
data class Todo(val userId: Int, val id: Int, val title: String, val completed: Boolean)

@Serializable
data class Post(val userId: Int, val id: Int, val title: String)

private fun upstreamHttpClient() = HttpClient(CIO) {
    expectSuccess = true
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 5_000
    }
}

class PlaceholderClient(private val baseUrl: String) : AutoCloseable {

    private val http get() = upstreamHttpClient()

    suspend fun users(fromId: Int, toId: Int): List<User> = get("/users?_start=$fromId&_end=$toId")

    suspend fun user(id: Int): User = get("/users/$id")

    suspend fun todos(userId: Int): List<Todo> = get("/todos?userId=$userId")

    suspend fun posts(userId: Int): List<Post> = get("/posts?userId=$userId")

    private suspend inline fun <reified T> get(path: String): T = http.get(baseUrl + path).body()

    override fun close() = http.close()
}
