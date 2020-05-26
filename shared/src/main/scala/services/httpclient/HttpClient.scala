package services.httpclient

import io.circe.{Decoder, Encoder}
import urldsl.language.PathSegment
import zio.{Task, ZIO}

object HttpClient {

  final class RequestFailed(val message: String) extends Exception(s"Error message: $message")

  trait Service {

    def get[R](path: PathSegment[Unit, _])(implicit decoder: Decoder[R]): Task[R]

    def post[B, R](path: PathSegment[Unit, _], body: B)(implicit encoder: Encoder[B], decoder: Decoder[R]): Task[R]

  }

}
