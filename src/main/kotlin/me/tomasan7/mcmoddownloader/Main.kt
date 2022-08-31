package me.tomasan7.mcmoddownloader

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.KebabCaseParamMapper
import com.sksamuel.hoplite.addResourceOrFileSource
import kotlinx.coroutines.*
import me.tomasan7.mcmoddownloader.data.Modrinth
import me.tomasan7.mcmoddownloader.data.project.Project
import me.tomasan7.mcmoddownloader.data.version.Version
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.fusesource.jansi.AnsiConsole
import java.io.File
import java.util.concurrent.ConcurrentHashMap


val logger: Logger = LogManager.getLogger(object {}.javaClass.enclosingClass)

suspend fun main()
{
    AnsiConsole.systemInstall()

    val configFile = File("./config.yml")

    if (!configFile.exists())
    {
        copyResourceIfNotExists(object
                                {}::class.java.enclosingClass.getResourceAsStream("/config.yml")!!, configFile)
        logger.info("Configuration doesn't exist, created config.yml. Fill it in.")
        return
    }

    val config = ConfigLoaderBuilder.default()
            .addResourceOrFileSource("./config.yml")
            .addParameterMapper(KebabCaseParamMapper)
            .build()
            .loadConfigOrThrow<Config>()

    val modsDir = File(config.outputDir).also { it.mkdir() }

    val dependencies = ConcurrentHashMap<Project, Set<Project>>()

    runBlocking {
        val mods = getMods(config.mods)

        for (mod in mods)
        {
            // To prevent rate limiting.
            delay(1000)
            val resultModVersion = decideVersion(Modrinth.getProjectVersions(mod, config.loaders, config.mcVersions), config.mcVersions)
            if (resultModVersion == null)
            {
                logger.warn("No suitable version found for ${mod.title}.")
                continue
            }

            val versionRequiredDependencies = getVersionRequiredDependencies(resultModVersion)
            dependencies[mod] = versionRequiredDependencies

            logger.info("Downloading ${mod.title}, version ${resultModVersion.version_number}...")
            downloadVersion(resultModVersion, modsDir, config.requestTimeoutMillis) { logger.info("Downloaded ${mod.title}, version ${resultModVersion.version_number}.") }
        }
    }

    runBlocking {
        val allDependencies = dependencies.values.flatten().toSet()
        val dependenciesFinal = mutableMapOf<Project, Set<Project>>()

        for (dependency in allDependencies)
            dependenciesFinal[dependency] = dependencies.filter { it.value.contains(dependency) }.keys

        for (dependency in dependenciesFinal.keys)
        {
            // To prevent rate limiting.
            delay(1000)
            val resultDependencyVersion = decideVersion(Modrinth.getProjectVersions(dependency, config.loaders, config.mcVersions), config.mcVersions)
            val requiredBy = dependenciesFinal[dependency]!!
            val requiredByStr = requiredBy.joinToString { it.title }
            if (resultDependencyVersion == null)
            {
                logger.warn("No suitable version found for dependency ${dependency.title} required by $requiredByStr.")
                continue
            }

            logger.info("Downloading dependency ${dependency.title}, version ${resultDependencyVersion.version_number}, required by $requiredByStr...")
            downloadVersion(resultDependencyVersion, modsDir, config.requestTimeoutMillis) {
                logger.info("Downloaded dependency ${dependency.title}, version ${resultDependencyVersion.version_number}, required by $requiredByStr.")
            }
        }
    }

    logger.info("Done.")
}

suspend fun getMods(slugsOrIds: Set<String>): Set<Project> = coroutineScope {
    val projectsDeferreds = mutableMapOf<String, Deferred<Project?>>()

    for (slugOrId in slugsOrIds)
        projectsDeferreds[slugOrId] = async { Modrinth.getProject(slugOrId) }

    return@coroutineScope projectsDeferreds.mapNotNull {
        val slugOrId = it.key
        val project = it.value.await()

        if (project == null)
        {
            logger.warn("No project found for slug/id '$slugOrId'")
            return@mapNotNull null
        }
        else if (project.project_type != "mod")
        {
            logger.warn("Project with slug/id '$slugOrId' is not a mod.")
            return@mapNotNull null
        }

        project
    }.toSet()
}

fun decideVersion(versions: List<Version>, targetGameVersions: List<String>): Version?
{
    // TODO: Improve this function

    if (versions.isEmpty())
    //println { "No version found for $mod, skipping it.".yellow }
        return null

    val versionMap = mutableMapOf<String, List<Version>>()

    for (mcVersion in targetGameVersions)
    {
        val versionsForMcVersion = versions.filter { it.game_versions.contains(mcVersion) }
        versionMap[mcVersion] = versionsForMcVersion
    }

    var resultVersion: Version? = null

    for (mcVersion in targetGameVersions)
    {
        val versionsForMcVersion = versionMap[mcVersion]

        if (versionsForMcVersion.isNullOrEmpty())
            continue

        // Latest
        resultVersion = versionsForMcVersion[0]
        break
    }

    /*if (resultVersion == null)
    {
        println { "No version found for $mod, skipping it.".yellow }
        return
    }*/

    return resultVersion
}

suspend fun getVersionRequiredDependencies(version: Version): Set<Project> = coroutineScope {
    val dependenciesProjectsDeferreds = mutableSetOf<Deferred<Project>>()

    for (dependency in version.dependencies)
        when
        {
            /* After some testing, it seems that both project_id and version_id cannot be null. */
            dependency.dependency_type != "required" -> continue
            dependency.project_id != null            -> dependenciesProjectsDeferreds.add(async { Modrinth.getProject(dependency.project_id)!! })
            dependency.version_id != null            -> dependenciesProjectsDeferreds.add(async { Modrinth.getVersionProject(dependency.version_id)!! })
        }

    val dependenciesProjects = dependenciesProjectsDeferreds.awaitAll()

    return@coroutineScope dependenciesProjects.toSet()
}

suspend fun downloadVersion(
    version: Version, destinationDir: File,
    requestTimeoutMillis: Long,
    onComplete: () -> Unit = {})
{
    val versionFile = version.files.find { it.primary } ?: run {
        logger.warn("No primary file found for ${version.name}.")
        return
    }

    Modrinth.downloadFile(versionFile.url, File(destinationDir, versionFile.filename), requestTimeoutMillis)
    onComplete()
}