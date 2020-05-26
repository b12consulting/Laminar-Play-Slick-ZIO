package services.database

import models.LoginData
import zio.{ZIO, ZLayer}

// Use H2Profile to connect to an H2 database
import slick.jdbc.H2Profile.api._
import slick.jdbc.H2Profile
import slick.lifted.Tag
import zio.Has
import services.crypto.HashedPassword

object BDatabase {

  /**
    * Table representation for Slick
    */
  private class UsersTable(tag: Tag) extends Table[LoginData](tag, "users") {
    def userName       = column[String]("user_name")
    def hashedPassword = column[String]("hashed_password")

    def * = (userName, hashedPassword) <> (LoginData.tupled, LoginData.unapply)
  }
  private def query: TableQuery[UsersTable] = TableQuery[UsersTable]

  /**
    * Live implementation of the Database service. Note that we are requiring a database object that is
    * specific to H2Profile. This guy will be provided automatically by play in the controllers.
    */
  val live = ZLayer.fromFunction { db: Has[H2Profile#Backend#DatabaseDef] =>
    new services.database.Database.Service {
      def findUser(userName: String): ZIO[Any, Throwable, Option[LoginData]] =
        ZIO.fromFuture { implicit ec =>
          db.get.run(
            query.filter(_.userName === userName).take(1).result.headOption
          )
        }

      def insertUser(userName: String, hashedPassword: HashedPassword): ZIO[Any, Throwable, Int] =
        ZIO.fromFuture { implicit ec =>
          db.get.run(
            query += LoginData(userName, hashedPassword.pw)
          )
        }
    }
  }

}
