import sbt.Keys._
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}


name := "Laminar-Play-ZIO"

version := "0.1"

val scalaCompilerOptions = List(
  "-deprecation",
  "-feature"
)

scalaVersion in ThisBuild := "2.13.1"
testFrameworks in ThisBuild += new TestFramework("zio.test.sbt.ZTestFramework")
scalacOptions in ThisBuild := scalaCompilerOptions

val circeVersion = "0.13.0"
val zioVersion   = "1.0.0-RC19-2"

lazy val `shared` = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio" %%% "zio" % zioVersion,
      "dev.zio" %%% "zio-streams" % zioVersion
    ) ++ Seq( // circe for json serialisation
      "io.circe" %%% "circe-core",
      "io.circe" %%% "circe-generic",
      "io.circe" %%% "circe-parser",
      "io.circe" %%% "circe-shapes",
      "io.circe" %%% "circe-generic-extras"
    ).map(_ % circeVersion) ++ Seq(
      "dev.zio" %%% "zio-test" % zioVersion % "test",
      "dev.zio" %%% "zio-test-sbt" % zioVersion % "test"
    ) ++ Seq(
      "be.doeraene" %%% "url-dsl" % "0.2.0"
    )
  )
  .jsSettings(
    libraryDependencies ++= Seq(
      // java.time library support for Scala.js
      "io.github.cquiroz" %%% "scala-java-time-tzdb" % "2.0.0"
    )
  )


lazy val `backend` = (project in file("./backend"))
  .enablePlugins(PlayScala)
  .settings(
    libraryDependencies ++= Seq(
      // binding of slick for play
      "com.typesafe.play" %% "play-slick" % "4.0.2",
      // handle db connection pool
      "com.typesafe.slick" %% "slick-hikaricp" % "3.3.2",
      // db evolutions
      evolutions,
      "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0",
      // dependency injection
      guice,
      // in memory database for illustration purposes
      "com.h2database" % "h2" % "1.4.200",
      // BCrypt library for hashing password
      "org.mindrot" % "jbcrypt" % "0.3m"
    )
  )
  .dependsOn(shared.jvm)

lazy val `frontend` = (project in file("./frontend"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    copyFrontendFastOpt := {
      (fastOptJS in Compile).value.data
    },
    libraryDependencies ++= Seq(
      "com.raquo" %%% "laminar" % "0.9.0"
    )
  )
  .dependsOn(shared.js)

val copyFrontendFastOpt = taskKey[File]("Return main process fast compiled file directory.")
lazy val fastOptCompileCopy = taskKey[Unit]("Compile and copy paste projects and generate corresponding json file.")
val copyPath: String = "backend/public/"

fastOptCompileCopy := {
  val frontendDirectory = (copyFrontendFastOpt in `frontend`).value
  IO.copyFile(frontendDirectory, baseDirectory.value / copyPath / "frontend-scala.js")
  IO.copyFile(
    frontendDirectory.getParentFile / "frontend-fastopt.js.map",
    baseDirectory.value / copyPath / "frontend-fastopt.js.map"
  )
}
