plugins {
  id("com.gradleup.shadow") version "8.3.8"
}

defaultMinecraft {}

dependencies {
  // Specify the version of Minecraft to use.
  // Any artifact can be supplied so long as it has a "userdev" classifier artifact and is a compatible patcher artifact.
  // The "userdev" classifier will be requested and setup by ForgeGradle.
  // If the group id is "net.minecraft" and the artifact id is one of ["client", "server", "joined"],
  // then special handling is done to allow a setup of a vanilla dependency without the use of an external repository.
  minecraft("net.minecraftforge:forge:${minecraft_version}-${forge_version}")

  implementation(fg.deobf(project(":netmix")))

  // netty quic
  val nettyQuicVersion = "0.0.72.Final"
  minecraftLibrary("io.netty.incubator:netty-incubator-codec-classes-quic:$nettyQuicVersion")
  minecraftLibrary("io.netty.incubator:netty-incubator-codec-native-quic:$nettyQuicVersion:linux-x86_64")
  minecraftLibrary("io.netty.incubator:netty-incubator-codec-native-quic:$nettyQuicVersion:linux-aarch_64")
  minecraftLibrary("io.netty.incubator:netty-incubator-codec-native-quic:$nettyQuicVersion:osx-x86_64")
  minecraftLibrary("io.netty.incubator:netty-incubator-codec-native-quic:$nettyQuicVersion:osx-aarch_64")
  minecraftLibrary("io.netty.incubator:netty-incubator-codec-native-quic:$nettyQuicVersion:windows-x86_64")
}

tasks.build {
  dependsOn(tasks.shadowJar)
}

tasks.shadowJar {
  dependencies {
    exclude(dependency("^(?!io.netty.incubator).*:.*:.*"))
  }

  finalizedBy("reobfShadowJar")
}

reobf {
  create("shadowJar")
}
