package me.tomasan7.mcmoddownloader.data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.tomasan7.mcmoddownloader.data.project.Project
import me.tomasan7.mcmoddownloader.data.version.Version
import java.io.File

object Modrinth
{
    private const val ENDPOINT: String = "https://api.modrinth.com/v2"

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        install(HttpTimeout)
    }

    suspend fun getProject(projectSlugOrId: String) = try
    {
        client.get("$ENDPOINT/project/$projectSlugOrId").body<Project>()
    }
    catch (e: NoTransformationFoundException)
    {
        null
    }

    suspend fun getProjectVersions(
        projectSlugOrId: String,
        loaders: List<String>? = null,
        gameVersions: List<String>? = null
    ) = client.get("$ENDPOINT/project/$projectSlugOrId/version") {
        url {
            if (!loaders.isNullOrEmpty())
                parameters.append("loaders", Json.encodeToString(loaders))
            if (!gameVersions.isNullOrEmpty())
                parameters.append("game_versions", Json.encodeToString(gameVersions))
        }
    }.body<List<Version>>()

    suspend fun getProjectVersions(
        project: Project,
        loaders: List<String>? = null,
        gameVersions: List<String>? = null
    ) = getProjectVersions(project.id, loaders, gameVersions)

    suspend fun getVersionProject(version: Version) = getProject(version.project_id)

    suspend fun getVersionProject(versionId: String) = getVersionProject(getVersion(versionId))

    suspend fun getVersion(versionId: String) = client.get("$ENDPOINT/version/$versionId").body<Version>()

    @OptIn(InternalAPI::class)
    suspend fun downloadFile(url: String, file: File, requestTimeoutMillis: Long = 10000)
    {
        client.get(url) {
            timeout {
                this.requestTimeoutMillis = requestTimeoutMillis
            }
        }.content.copyAndClose(file.writeChannel())
    }
}