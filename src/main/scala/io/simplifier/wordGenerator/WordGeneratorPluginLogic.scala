package io.simplifier.wordGenerator

import io.simplifier.pluginbase.interfaces.{DefaultConfigurationInterfaceService, DocumentationInterfaceService, PluginBaseHttpService, SlotInterfaceService}
import io.simplifier.pluginbase.permission.PluginPermissionObject
import io.simplifier.pluginbase.{SimplifierPlugin, SimplifierPluginLogic}
import io.simplifier.wordGenerator.controller.{CustomWordTemplateController, DefaultWordTemplateController}
import io.simplifier.wordGenerator.interfaces.WordGeneratorSlotInterface
import io.simplifier.wordGenerator.permission.{PermissionHandler, WordGeneratorPluginPermission}

import scala.concurrent.Future


abstract class WordGeneratorPluginLogic extends SimplifierPluginLogic(Defaults.PLUGIN_DESCRIPTION_DEFAULT, "wordGeneratorPlugin") {

  import ACTOR_SYSTEM.dispatcher

  val permission = WordGeneratorPluginPermission

  /**
   * The plugin permissions
   *
   * @return a sequence of the plugin permissions
   */
  override def pluginPermissions: Seq[PluginPermissionObject] = Seq(permission)

  /**
   * Starts the plugin service
   *
   * @param basicState the basic state containing all relevant information
   * @return Future containing
   */
  override def startPluginServices(basicState: SimplifierPlugin.BasicState): Future[PluginBaseHttpService] = Future {
    val permissionHandler = new PermissionHandler(basicState.dispatcher, basicState.settings)
    val slotInterface = Some(new WordGeneratorSlotInterface(basicState.dispatcher, basicState.pluginDescription, permission,
      new CustomWordTemplateController(permissionHandler), new DefaultWordTemplateController(permissionHandler), permissionHandler))

    val proxyInterface = None

    val configInterface = Some(new DefaultConfigurationInterfaceService("", "assets/", Seq()))

    val documentationInterface = Some(new DocumentationInterfaceService {
      override val apiClasses: Set[Class[_]] = Set(
        classOf[SlotInterfaceService.Documentation]
      )
      override val title: String = "WordGenerator Plugin Client API"
      override val description: String = "Plugin to generate word documents by use of placeholders and template files."
      override val externalDocsDescription: String = "Documentation for WordGenerator Plugin"
    })

    new PluginBaseHttpService(basicState.pluginDescription, basicState.settings, basicState.appServerInformation,
      proxyInterface, slotInterface, configInterface, documentationInterface)
  }

}
