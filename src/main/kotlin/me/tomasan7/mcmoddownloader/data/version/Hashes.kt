package me.tomasan7.mcmoddownloader.data.version

import kotlinx.serialization.Serializable

@Serializable
data class Hashes(
    val sha1: String,
    val sha512: String
)