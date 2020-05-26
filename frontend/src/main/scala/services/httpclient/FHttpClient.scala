package services.httpclient

import io.circe.{Decoder, Encoder}
import org.scalajs.dom
import org.scalajs.dom.raw.XMLHttpRequest
import urldsl.language.PathSegment
import zio.{Has, Layer, Task, UIO, ZIO, ZLayer}
import io.circe.parser.decode
import io.circe.syntax._
import HttpClient.RequestFailed

object FHttpClient {

  private final val csrfTokenName: String = "Csrf-Token"

  private def maybeCsrfToken: Option[String] =
    dom.document.cookie
      .split(";")
      .map(_.trim)
      .find(_.startsWith(s"$csrfTokenName="))
      .map(_.drop(csrfTokenName.length + 1))

  val live: Layer[Nothing, Has[HttpClient.Service]] =
    ZLayer.succeed(new HttpClient.Service {
      private def send[R](method: String, path: PathSegment[Unit, _], body: Option[String])(
          implicit decoder: Decoder[R]
      ) =
        for {
          request <- UIO(new XMLHttpRequest)
          responseTextFiber <- ZIO
            .effectAsync[Any, Throwable, R] { callback =>
              request.onreadystatechange = (_: dom.Event) => {
                if (request.readyState == 4 && request.status == 200) {
                  callback(
                    ZIO.fromEither(
                      decode[R](request.response.asInstanceOf[String])
                    )
                  )
                } else if (request.readyState == 4) {
                  callback(ZIO.fail(new RequestFailed(request.response.asInstanceOf[String])))
                }
              }
            }
            .fork
          _ <- ZIO.effectTotal {
            request.open(
              method,
              dom.document.location.origin.toString + "/" + path.createPath(),
              async = true
            )

            // Adding the csrf token if it is present
            maybeCsrfToken.foreach(request.setRequestHeader(csrfTokenName, _))

            body match {
              case Some(b) =>
                request.setRequestHeader("Content-Type", "application/json;charset=UTF-8")
                request.send(b)
              case None => request.send()
            }
          }
          response <- responseTextFiber.join
        } yield response

      def get[R](
          path: PathSegment[Unit, _]
      )(implicit decoder: Decoder[R]): Task[R] =
        send("GET", path, None)

      def post[B, R](
          path: PathSegment[Unit, _],
          body: B
      )(implicit encoder: Encoder[B], decoder: Decoder[R]): Task[R] =
        send("POST", path, Some(body.asJson.noSpaces))
    })

}
