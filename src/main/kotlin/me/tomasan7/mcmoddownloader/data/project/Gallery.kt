package me.tomasan7.mcmoddownloader.data.project

import kotlinx.serialization.Serializable

@Serializable
data class Gallery(
    val created: String,
    val description: String?,
    val featured: Boolean,
    val title: String?,
    val url: String
)