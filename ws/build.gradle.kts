import net.minecraftforge.gradle.userdev.tasks.JarJar
import org.gradle.toolchains.foojay.architectures32Bit

minecraft {
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
  mappings(
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
  copyIdeResources = true

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
  runs {
    // applies to all the run configs below
    configureEach {
      workingDirectory(project.file("run"))

      // Recommended logging data for a userdev environment
      // The markers can be added/remove as needed separated by commas.
      // "SCAN": For mods scan.
      // "REGISTRIES": For firing of registry events.
      // "REGISTRYDUMP": For getting the contents of all registries.
      property("forge.logging.markers", "REGISTRIES")


      // Recommended logging level for the console
      // You can set various levels here.
      // Please read: https://stackoverflow.com/questions/2031163/when-to-use-the-different-log-levels
      property("forge.logging.console.level", "debug")

      mods {
        create(mod_id) {
          source(sourceSets.main.get())
        }
      }
    }

    create("client") {
      // Comma-separated list of namespaces to load gametests from. Empty = all namespaces.
      project.extensions.extraProperties.set("forge.enabledGameTestNamespaces", mod_id)
    }

    create("server") {
      project.extensions.extraProperties.set("forge.enabledGameTestNamespaces", mod_id)
      args("--nogui")
    }

    // This run config launches GameTestServer and runs all registered gametests, then exits.
    // By default, the server will crash when no gametests are provided.
    // The gametest system is also enabled by default for other run configs under the /test command.
    create("gameTestServer") {
      property("forge.enabledGameTestNamespaces", mod_id)
    }

    create("data") {
      // example of overriding the workingDirectory set in configureEach above
      workingDirectory(project.file("run-data"))

      // Specify the modid for data generation, where to output the resulting resource, and where to look for existing resources.
      setArgs(
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
}

jarJar.enable()

dependencies {
  // Specify the version of Minecraft to use.
  // Any artifact can be supplied so long as it has a "userdev" classifier artifact and is a compatible patcher artifact.
  // The "userdev" classifier will be requested and setup by ForgeGradle.
  // If the group id is "net.minecraft" and the artifact id is one of ["client", "server", "joined"],
  // then special handling is done to allow a setup of a vanilla dependency without the use of an external repository.
  minecraft("net.minecraftforge:forge:${minecraft_version}-${forge_version}")

  minecraftLibrary(project(":netmix")) {
    exclude("net.minecraftforge")
  }

  val nettyVersion = "4.1.82.Final"
  minecraftLibrary("io.netty", "netty-codec-http", nettyVersion) {
    exclude("io.netty")
  }
  jarJar("io.netty", "netty-codec-http", "[$nettyVersion,$nettyVersion]") {
    exclude("io.netty")
  }
}
