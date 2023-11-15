package io.simplifier.msofficeadapter.word

import io.simplifier.msofficeadapter.MsOfficeAdapter.Replacements
import io.simplifier.msofficeadapter.word.templates.correcttemplates.CorrectTemplatesHandler
import io.simplifier.msofficeadapter.word.templates.filltemplates.FillTemplatesHandler

/**
  * Adapter to use for custom order of correcting and filling the templates
  */
protected[msofficeadapter] class CustomAdapter (fillTemplatesHandler: FillTemplatesHandler = new FillTemplatesHandler,
                                                correctTemplatesHandler: CorrectTemplatesHandler = new CorrectTemplatesHandler) {

  /**
    * Fills a Template with multiple replacements resulting in a List of filled templates (one for each replacement map)
    *
    * @param data the template as byte array
    * @param replacementList the replacements as List of string maps
    * @return a list of all filled templates as byte arrays
    */
  def fillTemplates(data: Array[Byte], replacementList: Seq[Replacements]): Seq[Array[Byte]] = {
    replacementList.map {
      replacements =>
        fillTemplatesHandler.handleTemplate(data, replacements.replacements, replacements.arrayReplacements)
    }
  }

  /**
    * Fills a template with a single replacement map
    *
    * @param data the template as byte array
    * @param replacements the replacements as string map
    * @return the filled template as a byte array
    */
  def fillTemplate(data: Array[Byte], replacements: Replacements): Array[Byte] = {
   fillTemplates(data, Seq(replacements)).headOption.getOrElse(Array.empty[Byte])
  }

  /**
    * Corrects the template concerning moustache placeholders being in different runs
    *
    * @param data the template as byte array
    * @return the corrected template as byte array
    */
  def correctTemplate(data: Array[Byte], replacements: Option[Replacements]): Array[Byte] = {
    correctTemplatesHandler.handleTemplate(data, replacements.map(_.replacements).getOrElse(Map()), replacements.map(_.arrayReplacements).getOrElse(Seq()))
  }
}