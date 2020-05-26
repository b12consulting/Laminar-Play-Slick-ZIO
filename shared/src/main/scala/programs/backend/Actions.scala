package programs.backend

import io.circe
import io.circe.parser.decode
import io.circe.generic.auto._
import models.LoginData
import zio.{UIO, ZIO}
import zio.duration.Duration
import services.crypto._
import services.database._
import zio.clock.Clock

import scala.concurrent.duration._

object Actions {

  /**
    * Retrieves the [[models.LoginData]] class from the body by deserializing it, and check that the given password
    * is correct.
    *
    * @param bodyAsStr body from frontend, representing the login data
    * @return Some(error message) if login information is not correct, and None if they are.
    */
  def handleLogin(bodyAsStr: String): ZIO[Database with Crypto with Clock, Throwable, Option[String]] =
    for {
      body <- ZIO.fromEither(decode[LoginData](bodyAsStr))
      maybeErrorMessage <- correctLogin(body.userName, body.password).as(Option.empty[String]).catchSome {
        case error: Database.WrongLoginInfo => UIO(Some(error.getMessage))
      }
      _ <- ZIO.sleep(Duration.fromScala(2.seconds)) // making look like it takes time.
    } yield maybeErrorMessage

  def handleRegister(bodyAsStr: String): ZIO[Database with Crypto, Throwable, Option[String]] =
    (for {
      body <- ZIO.fromEither(decode[LoginData](bodyAsStr))
      userAlreadyExistsFiber <- findUser(body.userName).map(_.isDefined).fork
      hashedPassword <- hashPassword(body.password)
      userAlreadyExists <- userAlreadyExistsFiber.join
      _ <- if (userAlreadyExists) ZIO.fail(new Database.WrongLoginInfo("User already exists")) else UIO.unit
      _ <- insertUser(body.userName, hashedPassword)
    } yield Option.empty[String])
      .catchSome {
        case error: Database.WrongLoginInfo => UIO(Some(error.getMessage))
      }

}
