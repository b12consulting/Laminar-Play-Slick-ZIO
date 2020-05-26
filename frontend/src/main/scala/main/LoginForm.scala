package main

import com.raquo.laminar.api.L._
import models.LoginData
import programs.frontend.FormSubmit
import zio.ZLayer
import zio.ZIO
import services.httpclient.FHttpClient
import services.httpclient.HttpClient

object LoginForm {

  final val layer = zio.console.Console.live ++ FHttpClient.live

  def apply(layer: ZLayer[Any, Nothing, zio.console.Console with HttpClient]) = {

    /**
      * Forms in Laminar typically works using "Event Sourcing". You start with an initial configuration, and you
      * update that configuration when the user interacts with the data.
      *
      * The `$loginData` Signal will emit at all times the current state of the form.
      */
    val changerBus = new EventBus[LoginData => LoginData]

    def nameChanger(newUsername: String): LoginData => LoginData     = _.copy(userName = newUsername)
    val nameObserver                                                 = changerBus.writer.contramap(nameChanger)
    def passwordChanger(newPassword: String): LoginData => LoginData = _.copy(password = newPassword)
    val passwordObserver                                             = changerBus.writer.contramap(passwordChanger)

    val $loginData = changerBus.events.fold(LoginData("", "")) { (currentLoginData, nextChanger) =>
      nextChanger(currentLoginData)
    }

    val submitBus = new EventBus[Unit]
    val $returnedLoginCall = submitBus.events
      .withCurrentValueOf($loginData)
      .map(_._2)
      .map(ZLayer.succeed(_))
      .map(_ ++ layer)
      .flatMap(
        layerWithLoginData =>
          EventStream.fromFuture(
            zio.Runtime.default.unsafeRunToFuture(
              FormSubmit.submitData(routes.loginRoute).provideLayer(layerWithLoginData)
            )
          )
      )

    val registerBus = new EventBus[Unit]
    val $returnedRegisterCall = registerBus.events
      .withCurrentValueOf($loginData)
      .map(_._2)
      .map(ZLayer.succeed(_))
      .map(_ ++ layer)
      .flatMap(
        layerWithLoginData =>
          EventStream.fromFuture(
            zio.Runtime.default.unsafeRunToFuture(
              FormSubmit.submitData(routes.registerRoute).provideLayer(layerWithLoginData)
            )
          )
      )

    div(
      form(
        onSubmit.preventDefault.mapTo(()) --> submitBus.writer,
        label("User name"),
        br(),
        input(`type` := "text", inContext(element => onChange.mapTo(element.ref.value) --> nameObserver)),
        br(),
        label("Password"),
        br(),
        input(`type` := "password", inContext(element => onChange.mapTo(element.ref.value) --> passwordObserver)),
        br(),
        input(`type` := "submit", value := "Login")
      ),
      button("Register", onClick.preventDefault.mapTo(()) --> registerBus),
      br(),
      child <-- EventStream.merge($returnedLoginCall, $returnedRegisterCall).map {
        case Left(errorMessage)    => span(color := "red", errorMessage)
        case Right(successMessage) => span(color := "green", successMessage)
      }
    )
  }

}
