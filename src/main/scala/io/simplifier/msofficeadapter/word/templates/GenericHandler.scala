package io.simplifier.msofficeadapter.word.templates

import io.simplifier.msofficeadapter.Exceptions.MsOfficeAdapterException
import org.apache.poi.xwpf.usermodel._
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR
import org.w3c.dom.{Node, NodeList}

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import scala.collection.JavaConverters._

/**
  * the abstract class which the correct and fill template handler extend
  */
private[msofficeadapter] abstract class GenericHandler {

  import GenericHandler._

  /**
    * Handles the template depending on whether to correct it or to fill it
    *
    * @param data         the template data as Array[Byte]
    * @param replacements the replacements consisting of a Map[String, String])
    * @return the corrected or filled template
    */
  def handleTemplate(data: Array[Byte], replacements: Map[String, String], arrayReplacements: Seq[Seq[Map[String, String]]]): Array[Byte] = {
    val doc = new XWPFDocument(new ByteArrayInputStream(data))
    try {
      iterateOverDocumentXMLFiles(doc, IterateInput(doc, replacements, arrayReplacements))
      writeOutput(doc)
    } catch {
      case ex: MsOfficeAdapterException => throw ex
      case ex: Throwable => throw MsOfficeAdapterException(s"Unexpected Exception ${ex.toString} while iterating over the document", ex)
    } finally {
      doc.close()
    }
  }

  /**
    * Method to implement what should happen with a XML-Part
    *
    * @param node           the Dom Node.
    * @param iterateInput   the iterate input consisting of the document and replacements as Map[String,String]
    * @return               the modified xml as an XMLObject
    */
  def handleXMLPart(node: Node, iterateInput: IterateInput): Unit = Unit

  def handleXMLPart(row: XWPFTable, iterateInput: IterateInput): Unit = Unit

  final private[msofficeadapter] def containsOnlyKeyReplacements(paragraph: XWPFParagraph, iterateInput: IterateInput): Boolean = {
    paragraph.getRuns.asScala.forall(textIsReplacementKey(_, iterateInput))
  }

  final private[msofficeadapter] def textIsReplacementKey(run: XWPFRun, iterateInput: IterateInput): Boolean = {
    iterateInput.replacements.keys.toSeq.exists(key => run.text().contains(iterateInput.placeHolder.separatorStart + s"$key" + iterateInput.placeHolder.separatorEnd))
  }

  protected[templates] def iterateOverDocumentXMLFiles(doc: XWPFDocument, iterateInput: IterateInput, filterPredicate: CTR => Boolean = (ctr: CTR) => true) : Seq[Seq[CTR]] = {

    for {
      tbl <- doc.getTables.asScala
    } yield {
      handleXMLPart(tbl, iterateInput)
    }

    val tableParagraphs: Seq[Seq[CTR]] = (for {
      tbl <- doc.getTables.asScala
      row <- tbl.getRows.asScala
      cell <- row.getTableCells.asScala
      paragraph <- cell.getParagraphs.asScala
    } yield {
      paragraph.getCTP.getRList.asScala.filter(filterPredicate)
    }).filter(_.nonEmpty)

    tableParagraphs.foreach(_.foreach(ctr => handleXMLPart(ctr.getDomNode, iterateInput)))

    val paragraphs: Seq[Seq[CTR]] = (for {paragraph <- doc.getParagraphs.asScala} yield {
      paragraph.getCTP.getRList.asScala.filter(filterPredicate)
    }).filter(_.nonEmpty)

    paragraphs.foreach(_.foreach(ctr => handleXMLPart(ctr.getDomNode, iterateInput)))

    val headerParagraphs: Seq[Seq[CTR]] = (for {
      header <- doc.getHeaderList.asScala
      paragraph <- header.getParagraphs.asScala
    } yield {
      paragraph.getCTP.getRList.asScala.filter(filterPredicate)
    }).filter(_.nonEmpty)

    headerParagraphs.foreach(_.foreach(ctr => handleXMLPart(ctr.getDomNode, iterateInput)))

    val headerTableParagraphs: Seq[Seq[CTR]] = (for {
      header <- doc.getHeaderList.asScala
      tbl <- header.getTables.asScala
      row <- tbl.getRows.asScala
      cell <- row.getTableCells.asScala
      paragraph <- cell.getParagraphs.asScala
    } yield {
      paragraph.getCTP.getRList.asScala.filter(filterPredicate)
    }).filter(_.nonEmpty)

    headerTableParagraphs.foreach(_.foreach(ctr => handleXMLPart(ctr.getDomNode, iterateInput)))

    val footerParagraphs: Seq[Seq[CTR]] = (for {
      footer <- doc.getFooterList.asScala
      paragraph <- footer.getParagraphs.asScala
    } yield {
      paragraph.getCTP.getRList.asScala.filter(filterPredicate)
    }).filter(_.nonEmpty)

    footerParagraphs.foreach(_.foreach(ctr => handleXMLPart(ctr.getDomNode, iterateInput)))

    val footerTableParagraphs: Seq[Seq[CTR]] = (for {
      footer <- doc.getFooterList.asScala
      tbl <- footer.getTables.asScala
      row <- tbl.getRows.asScala
      cell <- row.getTableCells.asScala
      paragraph <- cell.getParagraphs.asScala
    } yield {
      paragraph.getCTP.getRList.asScala.filter(filterPredicate)
    }).filter(_.nonEmpty)

    footerTableParagraphs.foreach(_.foreach(ctr => handleXMLPart(ctr.getDomNode, iterateInput)))

    tableParagraphs ++ paragraphs ++ headerParagraphs ++ headerTableParagraphs ++ footerParagraphs ++ footerTableParagraphs
  }

  def getNodes(node: Node): Seq[Node] = {
    def getNodes(node: Node, lstOfNodes: Seq[Node]): Seq[Node] = {
      if (!node.hasChildNodes) {
        if (!lstOfNodes.contains(node))
          add(lstOfNodes, node)
        else
          lstOfNodes
      } else {
        val childNodes: NodeList = node.getChildNodes
        val childNodeListLength: Int = Option(childNodes).map(_.getLength).getOrElse(0)
        for (i <- 0 to childNodeListLength) yield {
          if (childNodes.item(i) != null)
            getNodes(childNodes.item(i), add(lstOfNodes, childNodes.item(i)))
          else
            lstOfNodes
        }
      }.flatten
    }
    def add(lstOfNodes: Seq[Node], node: Node): Seq[Node] = {
        lstOfNodes ++ Seq(node)
    }
    getNodes(node, Seq[Node]())
  }

  protected[templates] def writeOutput(doc: XWPFDocument): Array[Byte] = {
    val output = new ByteArrayOutputStream()
    try {
      doc.write(output)
      output.toByteArray
    } catch {
      case ex: Throwable => throw MsOfficeAdapterException(s"Unexpected Exception ${ex.toString} while writing the document", ex)
    } finally {
      output.close()
    }
  }

}

object GenericHandler {

  case class IterateInput(
                            doc: XWPFDocument,
                            replacements: Map[String, String],
                            arrayReplacements: Seq[Seq[Map[String, String]]],
                            placeHolder: BlockIndicator = BlockIndicator("{{", "}}"),
                            blockHolder: BlockIndicator = BlockIndicator("[[", "]]")
                         )

  case class BlockIndicator(
                        separatorStart: String,
                        separatorEnd: String
                      ) {
    def getFirstCharacterOfSeparatorStart: Char = {
      separatorStart.charAt(0)
    }

    def getFirstCharacterOfSeparatorEnd: Char = {
      separatorEnd.charAt(0)
    }
  }

}