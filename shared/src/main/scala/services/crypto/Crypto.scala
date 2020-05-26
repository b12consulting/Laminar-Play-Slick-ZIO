package services.crypto

import zio.UIO

object Crypto {

  trait Service {

    def hashPassword(password: String): UIO[HashedPassword]

    def checkPassword(password: String, hashedPassword: HashedPassword): UIO[Boolean]

    /**
      * Validate the provided `maybePassword` against the `maybeHashedPassword`.
      * If `maybeHashedPassword` is None, then the password is assumed to be *not* required and always validated.
      */
    final def checkPasswordIfRequired(
        maybePassword: Option[String],
        maybeHashedPassword: Option[HashedPassword]
    ): UIO[Boolean] =
      (maybePassword, maybeHashedPassword) match {
        case (_, None)       => UIO(true)
        case (None, Some(_)) => UIO(false)
        case (Some(password), Some(hashedPassword)) =>
          checkPassword(password, hashedPassword)
      }

    def uuid: UIO[String]

  }

}
