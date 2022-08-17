package me.tomasan7.mcmoddownloader.data.version

import kotlinx.serialization.Serializable

@Serializable
data class Dependency(
    val dependency_type: String,
    val project_id: String?,
    val version_id: String?,
    val file_name: String?
)