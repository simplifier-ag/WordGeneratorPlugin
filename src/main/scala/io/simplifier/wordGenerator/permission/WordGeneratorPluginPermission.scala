package io.simplifier.wordGenerator.permission

import io.simplifier.pluginbase.permission.PluginPermissionObject
import io.simplifier.pluginbase.permission.PluginPermissionObjectCharacteristics.CheckboxCharacteristic

object WordGeneratorPluginPermission extends PluginPermissionObject {

  val characteristicUse = "use"

  val characteristicView = "view"

  val characteristicAdministrate = "administrate"

  /**
    * Name of the permission object.
    */
  override val name: String = "Word Generator Plugin"
  /**
    * Technical Name of the permission object.
    */
  override val technicalName: String = PluginPermissionObject.getTechnicalName("WordGenerator Plugin")
  /**
    * Description of the permission object.
    */
  override val description: String = "Plugin: Handle permissions for the Word Generator"
  /**
    * Possible characteristics for the admin ui.
    */
  override val characteristics: Seq[CheckboxCharacteristic] = Seq(
    CheckboxCharacteristic(characteristicUse, "Use", "Use the functionalities of the plugin"),
    CheckboxCharacteristic(characteristicView, "View", "View plugin content"),
    CheckboxCharacteristic(characteristicAdministrate, "Administrate", "Administrate the plugin")

  )
}
