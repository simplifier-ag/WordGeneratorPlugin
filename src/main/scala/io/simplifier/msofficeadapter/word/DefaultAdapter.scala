package io.simplifier.msofficeadapter.word

import io.simplifier.msofficeadapter.Exceptions.MsOfficeAdapterException
import io.simplifier.msofficeadapter.MsOfficeAdapter.Replacements

/**
  * Adapter to use for the default workflow of first correcting and then filling a template
  */
protected[msofficeadapter] class DefaultAdapter(customAdapter: CustomAdapter, pdfAdapter: PdfAdapter = new PdfAdapter) {

  /**
    * Corrects the template and then fills it multiple times with the given replacements
    *
    * @param data            the template as byte array
    * @param replacementList the replacements
    * @return a list of all filled templates as byte arrays
    */
  def fillTemplates(data: Array[Byte], replacementList: Seq[Replacements]): Seq[Array[Byte]] = {
    require(data != null, "The provided data may not be null.")
    require(replacementList != null, "The provided replacement list may not be null.")
    require(!replacementList.contains(null), "The provided replacement list may not contain null as replacement keys.")

    val correctedTemplate = customAdapter.correctTemplate(data, replacementList.headOption)

    if (correctedTemplate == null) {
      throw MsOfficeAdapterException("The corrected template was null", null)
    }

    customAdapter.fillTemplates(correctedTemplate, replacementList)
  }

  /**
    * Corrects the template and then fills it with a single replacement map
    *
    * @param data         the template as byte array
    * @param replacements the replacements
    * @return the filled template as a byte array
    */
  def fillTemplate(data: Array[Byte], replacements: Replacements): Array[Byte] = {
    fillTemplates(data, Seq(replacements)).head
  }

  /**
    * Corrects and fills the template with the replacements and converts the results to pdf
    *
    * @param data            the template as byte array
    * @param replacementList the replacements
    * @return a list of all filled templates in pdf format as byte arrays
    */
  def createPdfsFromTemplate(data: Array[Byte], replacementList: Seq[Replacements]): Seq[Array[Byte]] = {
    fillTemplates(data, replacementList).map(pdfAdapter.convertToPdf)
  }

  /**
    * Corrects and fills the template with the replacements and converts the result to pdf
    *
    * @param data the template as byte array
    * @param replacements the replacements
    * @return the filled template as pdf
    */
  def createPdfFromTemplate(data: Array[Byte], replacements: Replacements): Array[Byte] = {
    val filledTemplate = fillTemplate(data, replacements)
    pdfAdapter.convertToPdf(filledTemplate)
  }
}