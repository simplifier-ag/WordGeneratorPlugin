package io.simplifier.msofficeadapter.word

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import fr.opensagres.poi.xwpf.converter.pdf.{PdfConverter, PdfOptions}
import org.apache.poi.xwpf.usermodel.XWPFDocument


class PdfAdapter {

  /**
    * Converts the input document data to a pdf file and returns the data of the file
    *
    * @param data the byte array containing the data of the doc file
    * @return the byte array containing the data of the converted pdf
    */
  def convertToPdf(data: Array[Byte]): Array[Byte] = {
    val doc = new XWPFDocument(new ByteArrayInputStream(data))
    val out = new ByteArrayOutputStream()
    try {
      val options: PdfOptions = PdfOptions.create
      PdfConverter.getInstance().convert(doc, out, options)
      out.toByteArray
    } catch {
      case ex: Throwable => throw ex
    } finally {
      doc.close()
      out.close()
    }
  }
}