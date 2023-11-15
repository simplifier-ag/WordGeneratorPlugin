package io.simplifier.wordGenerator

import io.simplifier.pluginbase.SimplifierPlugin



object WordGeneratorPlugin extends WordGeneratorPluginLogic with SimplifierPlugin {
  val pluginSecret: String = byDeployment.PluginRegistrationSecret()
}
