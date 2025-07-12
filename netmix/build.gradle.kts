import org.spongepowered.asm.gradle.plugins.MixinExtension

buildscript {
  repositories {
    // These repositories are only for Gradle plugins, put any other repositories in the repository block further below
    maven { url = uri("https://repo.spongepowered.org/repository/maven-public/") }
    mavenCentral()
  }
  dependencies {
    classpath("org.spongepowered:mixingradle:0.7-SNAPSHOT")
  }
}

plugins {
  id("eclipse")
  id("idea")
}

apply(plugin = "org.spongepowered.mixin")


minecraft {
  mappings(
    mapOf(
      "channel" to mapping_channel,
      "version" to mapping_version,
    )
  )

  copyIdeResources = true
}

val Project.mixin: MixinExtension
  get() = extensions.getByType()

mixin.run {
  add(sourceSets.main.get(), "${mod_id}.refmap.json")

  config("${mod_id}.mixins.json")
}

dependencies {
  minecraft("net.minecraftforge:forge:${minecraft_version}-${forge_version}")
  annotationProcessor("org.spongepowered:mixin:0.8.5:processor")

  val mixinextrasVersion = "0.4.1"
  compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:$mixinextrasVersion")!!)
  implementation(jarJar("io.github.llamalad7:mixinextras-forge:$mixinextrasVersion")) {
    jarJar.ranged(this, "[$mixinextrasVersion,)")
  }
}
