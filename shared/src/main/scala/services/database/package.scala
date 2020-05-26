package services

import zio.Has
import zio.ZIO
import services.crypto.Crypto
import services.crypto.HashedPassword
import models.LoginData

package object database {

  type Database = Has[Database.Service]

  def correctLogin(userName: String, submittedPassword: String): ZIO[Database with Crypto, Throwable, Boolean] =
    ZIO.accessM(_.get[Database.Service].correctLogin(userName, submittedPassword))

  def findUser(userName: String): ZIO[Database, Throwable, Option[LoginData]] =
    ZIO.accessM(_.get[Database.Service].findUser(userName))

  def insertUser(userName: String, hashedPassword: HashedPassword): ZIO[Database, Throwable, Int] =
    ZIO.accessM(_.get[Database.Service].insertUser(userName, hashedPassword))

}
