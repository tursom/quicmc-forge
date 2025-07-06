defaultMinecraft {
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
