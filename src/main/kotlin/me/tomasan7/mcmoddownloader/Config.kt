package me.tomasan7.mcmoddownloader

data class Config(
    val requestTimeoutMillis: Long,
    val outputDir: String,
    val mods: Set<String>,
    val loaders: List<String>,
    val mcVersions: List<String>
)