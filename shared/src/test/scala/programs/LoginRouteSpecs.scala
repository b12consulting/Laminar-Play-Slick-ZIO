package programs

import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import models.LoginData
import programs.backend.Actions
import programs.frontend.FormSubmit
import services.crypto.{Crypto, HashedPassword}
import services.httpclient.HttpClient
import urldsl.language.PathSegment
import zio._
import zio.test.Assertion._
import zio.test.{DefaultRunnableSpec, _}
import services.database.Database
import zio.test._
import zio.test.environment._

import scala.collection.mutable


object LoginRouteSpecs extends DefaultRunnableSpec {


    val name = "hello"
    val somePW = "some pw"

  val crytoMockup: Layer[Nothing, Has[Crypto.Service]] = ZLayer.succeed(new Crypto.Service {
    def hashPassword(password: String): UIO[HashedPassword] = UIO(HashedPassword(password))

    def checkPassword(password: String, hashedPassword: HashedPassword): UIO[Boolean] = UIO(password == hashedPassword.pw)

    def uuid: UIO[String] = UIO("some uuid")
  })

  val dbMockup: ZLayer[Any, Nothing, Database] = ZLayer.succeed(new Database.Service {

    val db: mutable.Map[String, HashedPassword] = mutable.Map.empty
    db += (name -> HashedPassword("some pw"))

  def insertUser(userName: String, hashedPassword: HashedPassword): ZIO[Any, Throwable, Int]= ZIO.effectTotal {
    db += (userName -> hashedPassword)
    1
  }

  def findUser(userName: String): ZIO[Any, Throwable, Option[LoginData]] = 
    UIO(db.get(userName).map(_.pw).map(LoginData(userName, _)))
  })

  def spec: ZSpec[_root_.zio.test.environment.TestEnvironment, Any] = suite("login")(
  testM("Login with correct credentials and unexisting user") {
    val httpMockup = ZLayer.succeed(new HttpClient.Service {
      def get[R](path: PathSegment[Unit, _])(implicit decoder: Decoder[R]): Task[R] =
        ZIO.fail(new Exception("This should not be the used route"))

      def post[B, R](path: PathSegment[Unit, _], body: B)(implicit encoder: Encoder[B], decoder: Decoder[R]): Task[R] =
        if (path.createPath() == routes.loginRoute.createPath()) {
          for {
            bodyAsStr <- UIO(encoder(body).noSpaces)
            response <- Actions.handleLogin(bodyAsStr).provideLayer(zio.clock.Clock.live ++ crytoMockup ++ dbMockup)
            .flatMap {
              case Some(message) => ZIO.fail(new HttpClient.RequestFailed(message))
              case None => UIO("Logged in!")
            }
            responseAsStr <- UIO(response.asJson.noSpaces)
            decodedResponse <- ZIO.fromEither(decode[R](responseAsStr))
          } yield decodedResponse
        } else {
          ZIO.fail(new Exception("Wrong route."))
        }
    })

    val loginProgram = FormSubmit.submitData(routes.loginRoute).provideSomeLayer[Has[LoginData] with zio.console.Console](httpMockup)

    val wrongUsername = "IDontExist"

    for {
      loginData <- UIO(LoginData(name, somePW))
      response <- loginProgram.provideSomeLayer[zio.console.Console](ZLayer.succeed(loginData))
      loginData2 <- UIO(LoginData(wrongUsername, "doesnt matter"))
      response2 <- loginProgram.provideSomeLayer[zio.console.Console](ZLayer.succeed(loginData2))
    } yield assert(response)(equalTo(Right("Logged in!"))) && assert(response2)(equalTo(Left(s"User `$wrongUsername` does not exist.")))
  }
  )

}
