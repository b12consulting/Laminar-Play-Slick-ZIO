# Laminar-Play-ZIO-Slick

This is a small showcase of what a project using Laminar, Play, ZIO and Slick could look like.

The technologies are for:

- [Laminar](https://github.com/raquo/Laminar): make the frontend (browser side) in Scala
- [Play](https://www.playframework.com/): Scala web framework
- [Slick](https://scala-slick.org/): library for communicating with the database
- [ZIO](https://zio.dev/): functional effect library for gluing all things together nicely.

## Run the project

Once sbt is installed on your machine, in sbt command line:

- `fastOptCompileCopy` compiles the frontend and puts the compiled file in the server public directory
- `backend/run` runs the server locally on `localhost:9000`.

With two sbt consoles open, you can have `backend/run` running in the first one and `~fastOptCompileCopy` in the other so that frontend is automatically recompiled on file changes.

## Run the tests

There is one example of a test case available in the shared project. Run `sharedJVM/test` to run them (It is useless to run the `sharedJS` test since they are exactly the same).

## Alternatives

All of the afore mentioned technologies could be replaced by similar others:

- Laminar: see also [scala-js-react](https://github.com/japgolly/scalajs-react) and [Slinky](https://slinky.dev/), or even [Laminar-cycle](https://github.com/vic/laminar_cycle)
- Play: see also [cask](http://www.lihaoyi.com/cask/), [akka-http](https://doc.akka.io/docs/akka-http/current/introduction.html) or [http4s](https://http4s.org/)
- Slick: see also [doobie](https://tpolecat.github.io/doobie/), [quill](https://getquill.io/)
- ZIO: see also [scalaz](https://scalaz.github.io/7/), [cats-effect](https://typelevel.org/cats-effect/)
