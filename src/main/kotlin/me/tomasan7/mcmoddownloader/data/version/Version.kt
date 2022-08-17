package me.tomasan7.mcmoddownloader.data.version

import kotlinx.serialization.Serializable

@Serializable
data class Version(
    val author_id: String,
    val changelog: String,
    val changelog_url: String?,
    val date_published: String,
    val dependencies: List<Dependency>,
    val downloads: Int,
    val featured: Boolean,
    val files: List<VersionFile>,
    val game_versions: List<String>,
    val id: String,
    val loaders: List<String>,
    val name: String,
    val project_id: String,
    val version_number: String,
    val version_type: String
)