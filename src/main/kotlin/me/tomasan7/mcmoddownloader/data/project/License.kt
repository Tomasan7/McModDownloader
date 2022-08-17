package me.tomasan7.mcmoddownloader.data.project

import kotlinx.serialization.Serializable

@Serializable
data class License(
    val id: String,
    val name: String,
    val url: String
)