package me.tomasan7.mcmoddownloader.data.version

import kotlinx.serialization.Serializable

@Serializable
data class VersionFile(
    val filename: String,
    val hashes: Hashes,
    val primary: Boolean,
    val size: Int,
    val url: String
)