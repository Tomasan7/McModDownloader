package me.tomasan7.mcmoddownloader

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import me.tomasan7.mcmoddownloader.data.project.Project

suspend fun main()
{
    /* Found at https://modrinth.com/settings/ */
    val authorizationToken = ""

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        install(HttpTimeout)
    }

    val followedProjects = client.get("https://api.modrinth.com/v2/user/Tomasan7/follows") {
        header(HttpHeaders.Authorization, authorizationToken)
    }.body<List<Project>>()

    followedProjects.forEach { println("- ${it.slug}") }
}