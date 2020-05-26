package programs.frontend

import models.LoginData
import services.httpclient._
import zio.{Has, UIO, ZIO}
import zio.console.{putStrLn, Console}
import io.circe.generic.auto._

object FormSubmit {

  type Route = urldsl.language.PathSegment[Unit, _]

  def submitData(route: Route): ZIO[Console with HttpClient with Has[LoginData], Throwable, Either[String, String]] =
    for {
      loginData <- ZIO.access[Has[LoginData]](_.get[LoginData])
      response <- post[LoginData, String](route, loginData).map(Right[String, String]).catchSome {
        case error: HttpClient.RequestFailed => UIO(Left[String, String](error.message))
      }
      _ <- putStrLn(s"Returned: $response")
    } yield response

}
