package controllers

import javax.inject._

import play.api.libs.json.Json
import play.api.mvc._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import models.LoginData
import slick.jdbc.JdbcProfile
import play.api.db.slick.HasDatabaseConfigProvider
import play.api.db.slick.DatabaseConfigProvider
import services.crypto.Crypto
import services.crypto.BCrypto
import services.database.BDatabase
import zio.ZLayer
import zio.UIO
import slick.jdbc.H2Profile
import programs.backend.Actions
import play.api.libs.json.JsPath.json

@Singleton
class HomeController @Inject()(
    cc: ControllerComponents,
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with HasDatabaseConfigProvider[H2Profile] {

  /**
    * Creating the ZIO layer ("dependency injection") for our routes in this controller.
    * In an actual scenario, this could be provided directly to the controller by DI and instantiated elsewhere.
    */
  val layer = BCrypto.live ++ (ZLayer.succeed(db) >>> BDatabase.live) ++ zio.clock.Clock.live

  def appSummary = Action {
    Ok(Json.obj("content" -> "Scala Play Angular Seed"))
  }

  def postTest = Action {
    Ok(Json.obj("content" -> "Post Request Test => Data Sending Success"))
  }

  def login: Action[AnyContent] = Action.async { request =>
    request.body.asJson match {
      case None => Future.successful(BadRequest("Body empty: missing login info"))
      case Some(jsonBody) =>
        zio.Runtime.default.unsafeRunToFuture(
          Actions
            .handleLogin(jsonBody.toString)
            .provideLayer(layer)
            .map {
              case None          => Ok(Json.toJson("Logged in!")) // no error, user is connected
              case Some(message) => BadRequest(Json.toJson(message)) // there was an error, notifying frontend
            }
        )
    }
  }

  def register: Action[AnyContent] = Action.async { request =>
    request.body.asJson match {
      case None => Future.successful(BadRequest("Body empty: missing register info"))
      case Some(jsonBody) =>
        zio.Runtime.default.unsafeRunToFuture(
          Actions
            .handleRegister(jsonBody.toString)
            .provideLayer(layer)
            .map {
              case None          => Ok(Json.toJson("Registered!")) // no error, user is notified
              case Some(message) => BadRequest(Json.toJson(message)) // there was an error, notifying frontend
            }
        )
    }
  }

}
