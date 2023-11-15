package io.simplifier.wordGenerator.controller

import io.simplifier.msofficeadapter.MsOfficeAdapter
import Formats._
import io.simplifier.wordGenerator.permission.WordGeneratorPluginPermission.characteristicUse
import io.simplifier.pluginapi.UserSession
import io.simplifier.pluginapi.helper.Base64Encoding
import io.simplifier.pluginapi.rest.PluginHeaders.RequestSource
import io.simplifier.wordGenerator.permission.PermissionHandler

import scala.util.Try

/**
  * Controller for the custom template controller.
  * Use, when the default order of correcting and then filling the template is not sufficient
  */
class CustomWordTemplateController(permissionHandler: PermissionHandler) extends Base64Encoding {

  /**
    * Corrects the template concerning moustache placeholder being split over multiple runs
    *
    * @param correctTemplateRequest the request containing the template as b64 encoded string
    * @return the corrected template as b64 encoded string
    */
  def correctTemplate(correctTemplateRequest: CorrectTemplateRequest)
                     (implicit userSession: UserSession, requestSource: RequestSource): Try[CorrectTemplateResponse] = Try {
    permissionHandler.checkAdditionalPermission(characteristicUse)
    CorrectTemplateResponse(
      encodeB64(
        MsOfficeAdapter.customAdapter.correctTemplate(
          decodeB64(correctTemplateRequest.b64EncodedData),correctTemplateRequest.replacements)))
  }

  /**
    * Fills the template with the replacements
    *
    * @param fillTemplatesRequest the request containing the template as b64 encoded string and the replacements as list of string maps
    * @return the filled templates as b64 encoded strings
    */
  def fillTemplates(fillTemplatesRequest: FillTemplatesRequest)
                   (implicit userSession: UserSession, requestSource: RequestSource): Try[FillTemplatesResponse] = Try {
    permissionHandler.checkAdditionalPermission(characteristicUse)
    FillTemplatesResponse(
      MsOfficeAdapter.customAdapter.fillTemplates(
        decodeB64(fillTemplatesRequest.b64EncodedData), fillTemplatesRequest.replacementList)
      .map(encodeB64))
  }

  /**
    * Fills the template with a single replacement map
    *
    * @param fillTemplateRequest the request containing the template as b64 encoded string and the replacements as string map
    * @return the filled template as b64 encoded string
    */
  def fillTemplate(fillTemplateRequest: FillTemplateRequest)
                  (implicit userSession: UserSession, requestSource: RequestSource): Try[FillTemplateResponse] = Try {
    permissionHandler.checkAdditionalPermission(characteristicUse)
    FillTemplateResponse(
      encodeB64(
        MsOfficeAdapter.customAdapter.fillTemplate(
          decodeB64(fillTemplateRequest.b64EncodedData), fillTemplateRequest.replacements)))
  }
}