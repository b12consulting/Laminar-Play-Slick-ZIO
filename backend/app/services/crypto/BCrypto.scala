package services.crypto

import java.util.UUID

import org.mindrot.jbcrypt.BCrypt
import zio.{Has, Layer, UIO, ZIO, ZLayer}

object BCrypto {

  /**
    * Live implementation of the Crypto service. This class uses a JVM-only library under the hood,
    * hence its implementation is within the `backend` project.
    */
  val live: Layer[Nothing, Has[Crypto.Service]] = ZLayer.succeed(new Crypto.Service {
    def hashPassword(password: String): UIO[HashedPassword] =
      ZIO.succeed(BCrypt.hashpw(password, BCrypt.gensalt(13))).map(HashedPassword)

    def checkPassword(password: String, hashedPassword: HashedPassword): UIO[Boolean] =
      ZIO.succeed(BCrypt.checkpw(password, hashedPassword.pw))

    def uuid: UIO[String] = ZIO.effectTotal(UUID.randomUUID().toString)
  })

}
