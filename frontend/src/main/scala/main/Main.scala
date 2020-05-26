package main

import zio.{UIO, ZIO, ZLayer}
import zio.console.putStrLn
import io.circe.generic.auto._
import models.LoginData
import org.scalajs.dom
import org.scalajs.dom.html
import programs.frontend.FormSubmit
import routes._
import services.httpclient._
import com.raquo.laminar.api.L._

object Main extends zio.App {

  final val layer = zio.console.Console.live ++ FHttpClient.live

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    (for {
      _ <- putStrLn("Hello from Scala.js!")
      _ <- ZIO.effectTotal(render(dom.document.getElementById("root").asInstanceOf[html.Div], LoginForm(layer)))
    } yield 0).provideSomeLayer[zio.ZEnv](FHttpClient.live)

}
