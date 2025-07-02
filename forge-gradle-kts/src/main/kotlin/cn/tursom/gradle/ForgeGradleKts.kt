package cn.tursom.gradle

import mod_group_id
import mod_version
import org.gradle.api.Plugin
import org.gradle.api.Project

class ForgeGradleKts : Plugin<Project> {
  override fun apply(target: Project) {
    target.group = target.mod_group_id
    target.version = target.mod_version
  }
}