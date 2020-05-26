package object routes {

  import urldsl.language.PathSegment.dummyErrorImpl._

  final val loginRoute    = root / "api" / "login"
  final val registerRoute = root / "api" / "register"

}
