plugins {
  kotlin("jvm") version "1.9.10"
  `java-gradle-plugin`
}

group = "cn.tursom"
version = "1.1-SNAPSHOT"

repositories {
  // These repositories are only for Gradle plugins, put any other repositories in the repository block further below
  maven { url = uri("https://repo.spongepowered.org/repository/maven-public/") }
  mavenCentral()
}

dependencies {
  compileOnly(kotlin("gradle-plugin"))
  compileOnly("org.spongepowered:mixingradle:0.7-SNAPSHOT")
  compileOnly("net.minecraftforge.gradle:ForgeGradle:6.0.36")
}

gradlePlugin {
  plugins {
    create("forge-gradle-kts") {
      id = "forge-gradle-kts"
      implementationClass = "cn.tursom.gradle.ForgeGradleKts"
    }
  }
}
