package io.simplifier.msofficeadapter.word.templates.filltemplates

import io.simplifier.msofficeadapter.Exceptions.MsOfficeAdapterException
import io.simplifier.msofficeadapter.word.templates.GenericHandler
import io.simplifier.msofficeadapter.word.templates.GenericHandler.IterateInput
import org.apache.poi.xwpf.usermodel.{XWPFDocument, XWPFTable}
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR
import org.w3c.dom.{Node, NodeList}

import java.io.ByteArrayInputStream
import scala.collection.JavaConverters._

/**
  * The implementation of the GenericHandler for filling the template
  */
private[msofficeadapter] class FillTemplatesHandler extends GenericHandler {

  private[msofficeadapter] def replaceText(text: String, iterateInput: IterateInput): String = {
    (text /: iterateInput.replacements) {
      case (txt, (key, value)) =>
        if (value == null) {
          txt.replace(iterateInput.placeHolder.separatorStart + key + iterateInput.placeHolder.separatorEnd, "null")
        } else {
          txt.replace(iterateInput.placeHolder.separatorStart + key + iterateInput.placeHolder.separatorEnd, value)
        }
    }
  }

  override def handleXMLPart(table: XWPFTable, iterateInput: IterateInput): Unit = {
    iterateInput.arrayReplacements.foreach { arrayReplacement =>
      arrayReplacement.find(_.nonEmpty).foreach { head =>
        val keys: List[String] = head.keys.toList.map(key => s"{{$key}}")
        val tableOpt: Option[XWPFTable] = Seq(table).find { t =>
          t.getRows.asScala.toList.flatMap(r =>
            r.getTableCells.asScala.flatMap(c =>
              c.getParagraphs.asScala.toList).map(_.getText).intersect(keys)).nonEmpty
        }
        tableOpt.foreach { t =>
          val templateRow = t.getRow(1).getTableCells.asScala.map { cell =>
            cell.getText.replace("{", "").replace("}", "")
          }.zipWithIndex
          t.removeRow(1)
          arrayReplacement.foreach { repl =>
            val row = t.createRow
            templateRow.foreach {
              case (key, index) =>
                row.getCell(index).setText(repl.getOrElse(key, ""))
            }
          }
        }
      }
    }
  }

  /**
    * Method to implement what should happen with a XML-Part
    *
    * @param node           the Dom Node.
    * @param iterateInput   the iterate input consisting of the document and replacements as Map[String,String]
    * @return               the modified xml as an XMLObject
    */
  override def handleXMLPart(node: Node, iterateInput: IterateInput): Unit = {
    Option(node).foreach{
      val text = replaceText(
        Option(node).fold("")(n => Option(n.getNodeValue).getOrElse("")),
        iterateInput)
      _.setNodeValue(text)
    }
    if (Option(node).fold(false)(_.hasChildNodes)) {
      val childNodes: NodeList = node.getChildNodes
      val childNodeListLength: Int = Option(childNodes).map(_.getLength).getOrElse(0)
      for (i <- 0 to childNodeListLength) {
        handleXMLPart(childNodes.item(i), iterateInput)
      }
    }
  }

  override def handleTemplate(data: Array[Byte], replacements: Map[String, String], arrayReplacements: Seq[Seq[Map[String, String]]]): Array[Byte] = {
    val doc = new XWPFDocument(new ByteArrayInputStream(data))
    try {
      val iterateInput = IterateInput(doc, replacements, arrayReplacements)

      def filterSeparatorStart(ctr:CTR): Boolean = ctr.xmlText().contains(iterateInput.placeHolder.separatorStart)
      iterateOverDocumentXMLFiles(doc, iterateInput, filterSeparatorStart)

      val lstOfCtrs: Seq[Seq[CTR]] = iterateOverDocumentXMLFiles(doc, iterateInput)
      val lstOfNodes: Seq[Node] = lstOfCtrs.flatMap(_.flatMap(ctr => {getNodes(ctr.getDomNode)}))

      removeBlockholder(lstOfNodes, iterateInput)

      //If there are still unmapped placeholder in document, these placeholder have to be removed.
      val lstOfNotNullNodes = lstOfNodes.filter(_.getNodeValue != null)
      lstOfNotNullNodes.zipWithIndex.foreach {
        case (node, index) => if(node.getNodeValue != null) {
          val placeHolderStart = iterateInput.placeHolder.getFirstCharacterOfSeparatorStart
          val placeHolderEnd = iterateInput.placeHolder.getFirstCharacterOfSeparatorEnd
          //Matches a placeholder expression. The (?s) is a flag for (.*) to include new line characters.
          val expression = s"(?s)\\$placeHolderStart\\$placeHolderStart.*?\\$placeHolderEnd\\$placeHolderEnd"
          if(node.getNodeValue.matches(s".*?$expression.*?")) {
            //If placeholder has leading and trailing whitespace, remove one of them
            node.setNodeValue(node.getNodeValue.replaceAll(s"( )$expression( )", " "))
            //Remove all remaining unmapped placeholder.
            node.setNodeValue(node.getNodeValue.replaceAll(expression , ""))
            /*  Remove duplicate whitespace between several nodes if placeholder is removed
                and the node before ends with a whitespace and the node after begins with a whitespace */
            if (index > 0 && index < lstOfNotNullNodes.length - 1) {
              val nodeBeforeCurrent = lstOfNotNullNodes(index - 1)
              val nodeAfterCurrent = lstOfNotNullNodes(index + 1)
              if (nodeBeforeCurrent.getNodeValue.endsWith(" ") && nodeAfterCurrent.getNodeValue.startsWith(" ")) {
                nodeBeforeCurrent.setNodeValue(nodeBeforeCurrent.getNodeValue.substring(0, nodeBeforeCurrent.getNodeValue.length - 1))
              }
            }
          }
        }
      }

      writeOutput(doc)
    } catch {
      case ex: MsOfficeAdapterException => throw ex
      case ex: Throwable => throw MsOfficeAdapterException(s"Unexpected Exception ${ex.toString} while iterating over the document", ex)
    } finally {
      doc.close()
    }
  }

  def removeBlockholder(lstOfNodes: Seq[Node], iterateInput: IterateInput): Unit = {
    for((node, index) <- lstOfNodes.zipWithIndex) {
      if(node.getNodeValue != null) {
        val hasStartFlag = node.getNodeValue.contains(iterateInput.blockHolder.separatorStart)

        //Search all nodes from start block tag to end block tag
        val lstOfBlockNodes = if(hasStartFlag) {
          //Get all remaining nodes with end tag
          val nodesWithEndFlag = lstOfNodes.slice(index, lstOfNodes.length).filter(e =>
            if(e.getNodeValue != null) {
              e.getNodeValue.contains(iterateInput.blockHolder.separatorEnd)
            } else {
              false
            }
          )

          //From all remaining nodes with end tag get the nearest node.
          if(nodesWithEndFlag.isEmpty) {
            throw new RuntimeException("Invalid template. Missing end of block expression.")
          } else {
            val endIndex = lstOfNodes.indexOf(nodesWithEndFlag.head)+1
            lstOfNodes.slice(index, endIndex).filter(_.getNodeValue != null)
          }
        } else Seq[Node]()

        //Check all nodes of block expression if any node has still placeholder.
        val lstOfBlockNodesWithPlaceholder = lstOfBlockNodes.filter(e => {
          if(e.getNodeValue != null) {
            e.getNodeValue.contains(iterateInput.placeHolder.separatorStart) || e.getNodeValue.contains(iterateInput.placeHolder.separatorEnd)
          } else {
            false
          }
        })

        //If there are still placeholder in a block expression, remove the whole block.
        if(lstOfBlockNodesWithPlaceholder.nonEmpty) {

          lstOfBlockNodes.foreach(e => {
            val hasStartFlag = e.getNodeValue.contains(iterateInput.blockHolder.separatorStart)
            val hasEndFlag = e.getNodeValue.contains(iterateInput.blockHolder.separatorEnd)

            //If the block expression has prepending text, this text has to be preserved.
            val textBeforeFlag = if (hasStartFlag) {
              e.getNodeValue.substring(0, e.getNodeValue.indexOf(iterateInput.blockHolder.separatorStart))
            } else ""

            //If the block expression has appending text, this text has to be preserved.
            val textAfterFlag = if (hasEndFlag) {
              e.getNodeValue.substring(e.getNodeValue.indexOf(iterateInput.blockHolder.separatorEnd) + iterateInput.blockHolder.separatorEnd.length)
            } else ""

            e.setNodeValue(textBeforeFlag + textAfterFlag)

            //The whole text of nodes between the start and end tag have to be removed.
            if (!hasStartFlag && !hasEndFlag) {
              e.setNodeValue("")
            }
          })

        } else {
          lstOfBlockNodes.foreach(e => e.setNodeValue(
            e.getNodeValue.replaceAll("\\[\\[", "").replaceAll("\\]\\]", "")
          ))
        }
      }
    }
  }
}
