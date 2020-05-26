package services.database

import zio.ZIO
import models.LoginData
import services.crypto._
import zio.UIO

object Database {

  /** Simple error for throwing during login. */
  final class WrongLoginInfo(message: String) extends Exception(message)

  trait Service {

    /**
      * Tries to insert a new user to the database.
      * The password here should already be hashed using the Crypto service.
      * This is left abstract as it has to actully connect to the database.
      *
      * @param userName name for the new user
      * @param hashedPassword their hashed password
      * @return 1 if it was added.
      */
    def insertUser(userName: String, hashedPassword: HashedPassword): ZIO[Any, Throwable, Int]

    /**
      * Tries to find the user with specified username in the database.
      * This is left abstract as it has to actually connect to the database.
      *
      * This method can be mocked in the tests, for example by a simple Map or List.
      *
      * @param userName name of the user to find
      * @return user information, or None if it does not exist.
      */
    def findUser(userName: String): ZIO[Any, Throwable, Option[LoginData]]

    /**
      * Check whether the information probided by the user to login are correct, i.e.,
      * the user exists in the database and the password matches.
      *
      * @param userName name of the user who tries to connect
      * @param password submitted password
      * @return true if the user exists and it is the current password, a failed effect
      * with a [[WrongLoginInfo]] otherwise. (Could also fail with a database exception)
      */
    def correctLogin(userName: String, password: String) =
      for {
        maybeUser <- findUser(userName)
        user <- maybeUser match {
          case Some(info) => UIO(info)
          case None       => ZIO.fail(new WrongLoginInfo(s"User `$userName` does not exist."))
        }
        isPasswordCorrect <- checkPassword(password, HashedPassword(user.password))
        _ <- if (!isPasswordCorrect) ZIO.fail(new WrongLoginInfo("Wrong password!")) else UIO.unit
      } yield true

  }

}
