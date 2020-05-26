package services

import zio.{Has, URIO, ZIO}

package object crypto {

  type Crypto = Has[Crypto.Service]

  def hashPassword(password: String): URIO[Crypto, HashedPassword] =
    ZIO.accessM(_.get[Crypto.Service].hashPassword(password))

  def checkPassword(password: String, hashedPassword: HashedPassword): URIO[Crypto, Boolean] =
    ZIO.accessM(_.get[Crypto.Service].checkPassword(password, hashedPassword))

  def checkPasswordIfRequired(
      maybePassword: Option[String],
      maybeHashedPassword: Option[HashedPassword]
  ): URIO[Crypto, Boolean] =
    ZIO.accessM(_.get[Crypto.Service].checkPasswordIfRequired(maybePassword, maybeHashedPassword))

  val uuid: URIO[Crypto, String] = ZIO.accessM(_.get[Crypto.Service].uuid)

}
