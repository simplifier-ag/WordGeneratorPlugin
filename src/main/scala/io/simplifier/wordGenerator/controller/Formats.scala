package io.simplifier.wordGenerator.controller

import io.simplifier.msofficeadapter.MsOfficeAdapter.Replacements
import io.simplifier.pluginbase.util.api.ApiMessage

object Formats {
  case class CorrectTemplateRequest(b64EncodedData: String,  replacements: Option[Replacements]) extends ApiMessage

  case class CorrectTemplateResponse(b64EncodedData: String, success: Boolean = true) extends ApiMessage

  case class FillTemplatesRequest(b64EncodedData: String, replacementList: Seq[Replacements]) extends ApiMessage

  case class FillTemplatesResponse(b64EncodedDataList: Seq[String], success: Boolean = true) extends ApiMessage

  case class FillTemplateRequest(b64EncodedData: String, replacements: Replacements) extends ApiMessage

  case class FillTemplateResponse(b64EncodedData: String, success: Boolean = true) extends ApiMessage
}