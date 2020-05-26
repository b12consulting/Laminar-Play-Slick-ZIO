val scalaJSVersion =
  Option(System.getenv("SCALAJS_VERSION")).getOrElse("1.1.0")

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJSVersion)

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.0.0")
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.0")
