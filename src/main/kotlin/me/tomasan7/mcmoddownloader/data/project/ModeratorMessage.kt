package me.tomasan7.mcmoddownloader.data.project

import kotlinx.serialization.Serializable

@Serializable
data class ModeratorMessage(
    val body: String?,
    val message: String
)