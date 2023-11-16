package io.simplifier.msofficeadapter.word.templates.correcttemplates

import io.simplifier.msofficeadapter.word.templates.GenericHandler
import io.simplifier.msofficeadapter.word.templates.GenericHandler.IterateInput
import org.w3c.dom.Node

import scala.util.Try

/**
  * The implementation of the GenericHandler for correcting the template
  */
private[msofficeadapter] class CorrectTemplatesHandler extends GenericHandler {

  /**
    * Method to implement what should happen with a XML-Part
    *
    * @param node           the Dom Node.
    * @param iterateInput   the iterate input consisting of the document and replacements as Map[String,String]
    * @return               the modified xml as an XMLObject
    */
  override def handleXMLPart(node: Node, iterateInput: IterateInput): Unit = {
    val nodeValue: String = Option(node).flatMap(n => Option(n.getNodeValue)).getOrElse("")
    if (nodeValue.contains(iterateInput.placeHolder.getFirstCharacterOfSeparatorStart)) {
      val paragraph: Option[Node] = getParagraphNode(node)
      val runs: Seq[(Node, String)] = paragraph.fold(Seq.empty[(Node, String)])(pn => getRunNodes(pn))
      merge(runs, iterateInput)
    }
    getChildNodeSequence(node).foreach(handleXMLPart(_, iterateInput))
  }

  private[this] def getParagraphNode(node: Node): Option[Node] = {
    Option(node)
      .flatMap(n => Option(n.getParentNode))
      .flatMap(pn => if (pn.getNodeName == "w:p") Some(node.getParentNode) else getParagraphNode(pn.getParentNode))
  }

  private[this] def getTextNode(node: Node): Option[(Node, String)] = {
    Option(node)
      .flatMap(n => if (n.getNodeName == "w:t") {
        Some(n.getFirstChild, Option(n.getFirstChild).map(_.getNodeValue).getOrElse(""))
      } else if (!n.hasChildNodes) None
      else {
        getChildNodeSequence(n)
          .find(node => getTextNode(node).nonEmpty)
          .flatMap(n => Some(n.getFirstChild, Option(n.getFirstChild).map(_.getNodeValue).getOrElse("")))
      })

  }

  private[this] def getRunNodes(node: Node): Seq[(Node, String)] = {
    Option(node)
      .map(n => if (n.hasChildNodes) {
        getChildNodeSequence(node) flatMap getTextNode
      } else Seq.empty[(Node, String)]).getOrElse(Seq.empty[(Node, String)])
  }

  private[this] def getChildNodeSequence(node: Node): Seq[Node] = {
    Option(node)
      .flatMap(n => Option(n.getChildNodes))
      .fold(Seq.empty[Node]) {cn =>
        for (i <- 0 to Try(cn.getLength).getOrElse(0))
          yield Option(cn).map(c => Try(c.item(i)).getOrElse(null)).orNull
        }
      .filter(_ != null)
  }

  private[this] def merge(runs: Seq[(Node, String)], iterateInput: IterateInput): Seq[String] = {
    val splitRuns: Seq[Seq[(Node, String)]] = splitRunArray(runs, iterateInput)
    val mergedRuns: Seq[String] = splitRuns.map { run =>
      val wholeText: String = run.foldLeft("") {
        case (acc, (_, text)) => acc + text
      }
      run.headOption match {
        case None => ""
        case Some((null, _)) => ""
        case Some((node, _)) =>
          run.foreach {
            case (n, _) => Option(n).foreach(
              _.setNodeValue("")
            )
          }
          node.setNodeValue(wholeText)
          node.getNodeValue
      }
    }
    mergedRuns
  }

  private[this] def splitRunArray(runs: Seq[(Node, String)], iterateInput: IterateInput): Seq[Seq[(Node, String)]] = {
    val filteredRuns: Seq[(Node, String)] = runs.filter {
      case (node, value) => node != null && value != null
    }
    val firstCharOfSeparatorStart = iterateInput.placeHolder.getFirstCharacterOfSeparatorStart.toString
    val firstCharOfSeparatorEnd = iterateInput.placeHolder.getFirstCharacterOfSeparatorEnd.toString
    val separatorStart = iterateInput.placeHolder.separatorStart
    val separatorEnd = iterateInput.placeHolder.separatorEnd
    val (startingPoints, endingPoints) = filteredRuns.zipWithIndex.map {
      case ((_,text), i) =>

        def checkIfSplittedSeparator(separator: String): Boolean = {
          i > 0 && text.startsWith(separator) && filteredRuns(i-1)._2.endsWith(separator)
        }

        val isSplittedStartingPoint = checkIfSplittedSeparator(firstCharOfSeparatorStart)

        val startingPoint = if(isSplittedStartingPoint)
          Some(i-1)
        else if(text.lastIndexOf(separatorStart) > text.lastIndexOf(separatorEnd)) {
          Some(i)
        } else {
          None
        }
        val separatorStartNotExisting = text.indexOf(separatorStart) == -1
        val separatorEndExists = text.indexOf(separatorEnd) > -1
        val separatorEndBeforeStart = text.indexOf(separatorEnd) < text.indexOf(separatorStart)

        val isSplittedEndingPoint = checkIfSplittedSeparator(firstCharOfSeparatorEnd)

        val endingPoint = if(isSplittedEndingPoint || separatorEndExists && (separatorEndBeforeStart || separatorStartNotExisting)) {
          Some(i)
        } else {
          None
        }
        (startingPoint, endingPoint)
    }.unzip

    val diffStartingPoints = startingPoints.flatten.diff(endingPoints.flatten)
    val diffEndingPoints = endingPoints.flatten.diff(startingPoints.flatten)

    diffStartingPoints.map { currentStartingPoint =>
      val nextEndpoint = diffEndingPoints.find(_ > currentStartingPoint).getOrElse(filteredRuns.length)
      filteredRuns.slice(currentStartingPoint, nextEndpoint + 1)
    }
  }

}

object CorrectTemplatesHandler {

  case class SearchClosingParenthesesResult(notFound: Option[Boolean], isLastElement: Option[Boolean], indexOfClosingParentheses: Option[Int])

}
