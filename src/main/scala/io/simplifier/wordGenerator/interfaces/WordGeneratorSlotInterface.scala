package io.simplifier.wordGenerator.interfaces

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import io.simplifier.wordGenerator.controller.Formats.{CorrectTemplateRequest, FillTemplateRequest, FillTemplatesRequest}
import io.simplifier.wordGenerator.permission.WordGeneratorPluginPermission.characteristicAdministrate
import io.simplifier.pluginapi.UserSession
import io.simplifier.pluginapi.rest.PluginHeaders
import io.simplifier.pluginapi.rest.PluginHeaders.RequestSource
import io.simplifier.pluginbase.PluginDescription
import io.simplifier.pluginbase.interfaces.{AppServerDispatcher, SlotInterfaceService}
import io.simplifier.pluginbase.permission.PluginPermissionObject
import io.simplifier.pluginbase.util.api.{ApiMessage, PredefinedApiFailures}
import io.simplifier.pluginbase.util.logging.Logging
import io.simplifier.wordGenerator.controller.{CustomWordTemplateController, DefaultWordTemplateController}
import io.simplifier.wordGenerator.permission.PermissionHandler
import org.json4s.{DefaultFormats, Formats, MappingException}

import scala.util.{Failure, Success, Try}

class WordGeneratorSlotInterface(dispatcher: AppServerDispatcher, pluginDescription: PluginDescription, pluginPermission: PluginPermissionObject,
                                 customWordTemplateController: CustomWordTemplateController,
                                 defaultWordTemplateController: DefaultWordTemplateController,
                                 permissionHandler: PermissionHandler)
                                (implicit system: ActorSystem, materializer: Materializer)
                                extends SlotInterfaceService(dispatcher, pluginDescription, pluginPermission) with Logging {

  implicit override val formats: Formats = DefaultFormats.lossless

  /**
    * Handles the response, so that the correct api messages are sent to the caller
    *
    * @param response the response of the controller function
    * @param action the action (used for logging)
    * @return an api message containing either an error or the response
    */
  private def responseHandler(response: Try[ApiMessage], action: String): ApiMessage = {
    response match {
      case Success(value) => value
      case Failure(ex: MappingException) =>
        throw PredefinedApiFailures.BadRequestFailure(ex.getMessage)
      case Failure(ex: Throwable) =>
        logger.error(s"Operation $action failed.", ex)
        throw PredefinedApiFailures.UnexpectedErrorFailure(s"Unexpected Exception while $action. " + ex.getMessage, ex)
    }
  }

  /** Base-URL relative to http service root */
  override val baseUrl: String = "slots"

  /** Available plugin slots */
  override def pluginSlotNames: Seq[String] = Seq("defaultFillTemplates", "defaultFillTemplate",
    "customFillTemplates", "customFillTemplate", "customCorrectTemplate", "createPdf", "createPdfs")

  override protected def checkAdministratePermission()(implicit userSession: UserSession, requestSource: RequestSource): Unit = {
    permissionHandler.checkAdditionalPermission(characteristicAdministrate)
  }

  /**
    *
    * @param requestSource  plugin request source
    * @param userSession    authenticated user session
    * @return service route
    */
  override def serviceRoute(implicit requestSource: PluginHeaders.RequestSource, userSession: UserSession): Route = {
    path("defaultFillTemplate") {
      entity(as[FillTemplateRequest]) { input =>
        val filledTemplate = defaultWordTemplateController.fillTemplate(input)
        complete(responseHandler(filledTemplate, "filled Template default"))
      }
    } ~
      path("defaultFillTemplates") {
        entity(as[FillTemplatesRequest]) { input =>
          val filledTemplates = defaultWordTemplateController.fillTemplates(input)
          complete(responseHandler(filledTemplates, "filled Templates default"))
        }
      } ~
      path("customFillTemplate") {
        entity(as[FillTemplateRequest]) { input =>
          val filledTemplate = customWordTemplateController.fillTemplate(input)
          complete(responseHandler(filledTemplate, "filled Template custom"))
        }
      } ~
      path("customFillTemplates") {
        entity(as[FillTemplatesRequest]) { input =>
          val filledTemplates = customWordTemplateController.fillTemplates(input)
          complete(responseHandler(filledTemplates, "filled Templates custom"))
        }
      } ~
      path("customCorrectTemplate") {
        entity(as[CorrectTemplateRequest]) { input =>
          val correctTemplateResponse = customWordTemplateController.correctTemplate(input)
          complete(responseHandler(correctTemplateResponse, "correct Template"))
        }
      } ~
      path("createPdf") {
        entity(as[FillTemplateRequest]) { input =>
          val filledPdf = defaultWordTemplateController.createPdf(input)
          complete(responseHandler(filledPdf, "created pdf"))
        }
      } ~
      path("createPdfs") {
        entity(as[FillTemplatesRequest]) { input =>
          val filledPdfs = defaultWordTemplateController.createPdfs(input)
          complete(responseHandler(filledPdfs, "created pdfs"))
        }
      }
  }
}