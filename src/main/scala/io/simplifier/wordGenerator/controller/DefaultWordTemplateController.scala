package io.simplifier.wordGenerator.controller

import io.simplifier.msofficeadapter.MsOfficeAdapter
import Formats.{FillTemplateRequest, FillTemplateResponse, FillTemplatesRequest, FillTemplatesResponse}
import io.simplifier.wordGenerator.permission.WordGeneratorPluginPermission.characteristicUse
import io.simplifier.pluginapi.UserSession
import io.simplifier.pluginapi.helper.Base64Encoding
import io.simplifier.pluginapi.rest.PluginHeaders.RequestSource
import io.simplifier.wordGenerator.controller.Formats.{FillTemplateRequest, FillTemplateResponse, FillTemplatesRequest, FillTemplatesResponse}
import io.simplifier.wordGenerator.permission.PermissionHandler

import scala.util.Try

/**
  * Controller for the default template controller.
  * Use when you want to correct the template prior to filling it
  */
class DefaultWordTemplateController(permissionHandler: PermissionHandler) extends Base64Encoding {

  /**
    * Corrects the template and then fills it with the replacements
    *
    * @param fillTemplatesRequest the request containing the template as b64 encoded string and the replacements as list of string maps
    * @return the filled templates as b64 encoded strings
    */
  def fillTemplates(fillTemplatesRequest: FillTemplatesRequest)
                   (implicit userSession: UserSession, requestSource: RequestSource): Try[FillTemplatesResponse] = Try {
    permissionHandler.checkAdditionalPermission(characteristicUse)
    FillTemplatesResponse(
      MsOfficeAdapter.defaultAdapter.fillTemplates(
        decodeB64(fillTemplatesRequest.b64EncodedData), fillTemplatesRequest.replacementList)
        .map(encodeB64))
  }

  /**
    * Corrects the template and then fills it with with a single replacement map
    *
    * @param fillTemplateRequest the request containing the template as b64 encoded string and the replacements as string map
    * @return the filled template as b64 encoded string
    */
  def fillTemplate(fillTemplateRequest: FillTemplateRequest)
                  (implicit userSession: UserSession, requestSource: RequestSource): Try[FillTemplateResponse] = Try {
    permissionHandler.checkAdditionalPermission(characteristicUse)
    FillTemplateResponse(
      encodeB64(
        MsOfficeAdapter.defaultAdapter.fillTemplate(
          decodeB64(fillTemplateRequest.b64EncodedData), fillTemplateRequest.replacements)))
  }

  /**
    * Corrects the template, fills it with the replacements and converts it to a pdf
    *
    * @param fillTemplatesRequest the request containing the template as b64 encoded string and the replacements
    * @return the pdfs as b64 encoded strings
    */
  def createPdfs(fillTemplatesRequest: FillTemplatesRequest)
                (implicit userSession: UserSession, requestSource: RequestSource): Try[FillTemplatesResponse] = Try {
    permissionHandler.checkAdditionalPermission(characteristicUse)
    FillTemplatesResponse(
      MsOfficeAdapter.defaultAdapter.createPdfsFromTemplate(
        decodeB64(fillTemplatesRequest.b64EncodedData), fillTemplatesRequest.replacementList
      ).map(encodeB64)
    )
  }

  /**
    * Corrects the template, fills it and converts it to a pdf
    *
    * @param fillTemplateRequest the request containing the template as b64 encoded string and the replacements
    * @return the pdf as b64 encoded string
    */
  def createPdf(fillTemplateRequest: FillTemplateRequest)
               (implicit userSession: UserSession, requestSource: RequestSource): Try[FillTemplateResponse] = Try {
    permissionHandler.checkAdditionalPermission(characteristicUse)
    FillTemplateResponse(
      encodeB64(
        MsOfficeAdapter.defaultAdapter.createPdfFromTemplate(
          decodeB64(fillTemplateRequest.b64EncodedData), fillTemplateRequest.replacements)
      )
    )
  }
}

