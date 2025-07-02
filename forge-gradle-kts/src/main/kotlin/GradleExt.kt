import net.minecraftforge.gradle.common.util.MinecraftExtension
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

inline fun <reified T> ExtensionContainer.getByNameAndType(name: String): T {
  return getByName(name) as T
}

val Project.mainSourceSet: SourceSet
  get() = extensions
    .getByNameAndType<SourceSetContainer>("sourceSets")
    .getByName("main")

fun Project.defaultMinecraft(configuration: MinecraftExtension.() -> Unit = {}) {
  extensions.configure(MinecraftExtension::class.java) { minecraft ->
    // The mappings can be changed at any time and must be in the following format.
    // Channel:   Version:
    // official   MCVersion             Official field/method names from Mojang mapping files
    // parchment  YYYY.MM.DD-MCVersion  Open community-sourced parameter names and javadocs layered on top of official
    //
    // You must be aware of the Mojang license when using the "official" or "parchment" mappings.
    // See more information here: https://github.com/MinecraftForge/MCPConfig/blob/master/Mojang.md
    //
    // Parchment is an unofficial project maintained by ParchmentMC, separate from MinecraftForge
    // Additional setup is needed to use their mappings: https://parchmentmc.org/docs/getting-started
    //
    // Use non-default mappings at your own risk. They may not always work.
    // Simply re-run your setup task after changing the mappings to update your workspace.
    minecraft.mappings(
      mapOf(
        "channel" to mapping_channel,
        "version" to mapping_version,
      )
    )

    // When true, this property will have all Eclipse/IntelliJ IDEA run configurations run the "prepareX" task for the given run configuration before launching the game.
    // In most cases, it is not necessary to enable.
    // enableEclipsePrepareRuns = true
    // enableIdeaPrepareRuns = true

    // This property allows configuring Gradle"s ProcessResources task(s) to run on IDE output locations before launching the game.
    // It is REQUIRED to be set to true for this template to function.
    // See https://docs.gradle.org/current/dsl/org.gradle.language.jvm.tasks.ProcessResources.html
    minecraft.copyIdeResources.set(true)

    // When true, this property will add the folder name of all declared run configurations to generated IDE run configurations.
    // The folder name can be set on a run configuration using the "folderName" property.
    // By default, the folder name of a run configuration is the name of the Gradle project containing it.
    // generateRunFolders = true

    // This property enables access transformers for use in development.
    // They will be applied to the Minecraft artifact.
    // The access transformer file can be anywhere in the project.
    // However, it must be at "META-INF/accesstransformer.cfg" in the final mod jar to be loaded by Forge.
    // This default location is a best practice to automatically put the file in the right place in the final jar.
    // See https://docs.minecraftforge.net/en/latest/advanced/accesstransformers/ for more information.
    // accessTransformer = file("src/main/resources/META-INF/accesstransformer.cfg")

    // Default run configurations.
    // These can be tweaked, removed, or duplicated as needed.
    minecraft.runs.apply {
      // applies to all the run configs below
      configureEach { run ->
        run.workingDirectory(project.file("run"))

        // Recommended logging data for a userdev environment
        // The markers can be added/remove as needed separated by commas.
        // "SCAN": For mods scan.
        // "REGISTRIES": For firing of registry events.
        // "REGISTRYDUMP": For getting the contents of all registries.
        run.property("forge.logging.markers", "REGISTRIES")


        // Recommended logging level for the console
        // You can set various levels here.
        // Please read: https://stackoverflow.com/questions/2031163/when-to-use-the-different-log-levels
        run.property("forge.logging.console.level", "debug")

        run.property("mixin.env.disableRefMap", "true")

        run.mods.apply {
          create(mod_id) { mod ->
            mod.source(mainSourceSet)
          }
        }
      }

      create("client") { run ->
        // Comma-separated list of namespaces to load gametests from. Empty = all namespaces.
        minecraft.project.extensions.extraProperties.set("forge.enabledGameTestNamespaces", mod_id)

        run.property("mixin.env.disableRefMap", "true")
      }

      create("server") { run ->
        minecraft.project.extensions.extraProperties.set("forge.enabledGameTestNamespaces", mod_id)
        run.args("--nogui")

        run.property("mixin.env.disableRefMap", "true")
      }

      // This run config launches GameTestServer and runs all registered gametests, then exits.
      // By default, the server will crash when no gametests are provided.
      // The gametest system is also enabled by default for other run configs under the /test command.
      create("gameTestServer") { run ->
        run.property("forge.enabledGameTestNamespaces", mod_id)

        run.property("mixin.env.disableRefMap", "true")
      }

      create("data") { run ->
        // example of overriding the workingDirectory set in configureEach above
        run.workingDirectory(project.file("run-data"))

        run.property("mixin.env.disableRefMap", "true")

        // Specify the modid for data generation, where to output the resulting resource, and where to look for existing resources.
        run.setArgs(
          listOf(
            "--mod",
            mod_id,
            "--all",
            "--output",
            file("src/generated/resources/").absolutePath,
            "--existing",
            file("src/main/resources/").absolutePath,
          )
        )
      }
    }

    configuration(minecraft)
  }
}

fun Project.propertyString(name: String): String {
  return findProperty(name) as? String
    ?: rootProject.findProperty(name) as? String
    ?: throw IllegalArgumentException("Property '$name' is not set.")
}

val Project.isBuildTask: Boolean
  get() = gradle.startParameter.taskNames.any { it.endsWith("build") }

@Suppress("PropertyName")
val Project.mod_id get() = propertyString("mod_id")

@Suppress("PropertyName")
val Project.mod_name get() = propertyString("mod_name")

@Suppress("PropertyName")
val Project.mod_license get() = propertyString("mod_license")

@Suppress("PropertyName")
val Project.mod_version get() = propertyString("mod_version")

@Suppress("PropertyName")
val Project.mod_authors get() = propertyString("mod_authors")

@Suppress("PropertyName")
val Project.mod_description get() = propertyString("mod_description")

@Suppress("PropertyName")
val Project.mod_group_id get() = propertyString("mod_group_id")

@Suppress("PropertyName")
val Project.mapping_channel get() = propertyString("mapping_channel")

@Suppress("PropertyName")
val Project.mapping_version get() = propertyString("mapping_version")

@Suppress("PropertyName")
val Project.minecraft_version get() = propertyString("minecraft_version")

@Suppress("PropertyName")
val Project.minecraft_version_range get() = propertyString("minecraft_version_range")

@Suppress("PropertyName")
val Project.forge_version get() = propertyString("forge_version")

@Suppress("PropertyName")
val Project.forge_version_range get() = propertyString("forge_version_range")

@Suppress("PropertyName")
val Project.loader_version_range get() = propertyString("loader_version_range")
