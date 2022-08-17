package me.tomasan7.mcmoddownloader.data.project

import kotlinx.serialization.Serializable

@Serializable
data class Project(
    val body: String,
    val body_url: String?,
    val categories: List<String>,
    val client_side: String,
    val description: String,
    val discord_url: String?,
    val donation_urls: List<DonationUrl>,
    val downloads: Int,
    val followers: Int,
    val gallery: List<Gallery>,
    val icon_url: String,
    val id: String,
    val issues_url: String?,
    val license: License,
    val moderator_message: ModeratorMessage?,
    val project_type: String,
    val published: String,
    val server_side: String,
    val slug: String,
    val source_url: String?,
    val status: String,
    val team: String,
    val title: String,
    val updated: String,
    val approved: String?,
    val versions: List<String>,
    val wiki_url: String?
)
{
    override fun hashCode() = id.hashCode()

    override fun equals(other: Any?): Boolean
    {
        if (other !is Project)
            return false

        return other.id == id
    }
}