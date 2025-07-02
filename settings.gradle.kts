pluginManagement {
  repositories {
    gradlePluginPortal()
    maven {
      name = "MinecraftForge"
      url = uri("https://maven.minecraftforge.net/")
    }
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}

rootProject.name = "quicmc"

includeBuild("forge-gradle-kts")
include("netmix")
include("quic")
include("ws")
