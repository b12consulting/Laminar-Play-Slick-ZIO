package services

import io.circe.{Decoder, Encoder}
import urldsl.language.PathSegment
import zio.{Has, ZIO}

package object httpclient {

  type HttpClient = Has[HttpClient.Service]

  def get[R](path: PathSegment[Unit, _])(implicit decoder: Decoder[R]): ZIO[HttpClient, Throwable, R] =
    ZIO.accessM(_.get[HttpClient.Service].get(path))

  def post[B, R](
      path: PathSegment[Unit, _],
      body: B
  )(implicit encoder: Encoder[B], decoder: Decoder[R]): ZIO[HttpClient, Throwable, R] =
    ZIO.accessM(_.get[HttpClient.Service].post(path, body))

}
