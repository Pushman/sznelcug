import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  import Versions._
  import Resolvers._

  val appName = "sznelcug"
  val appVersion = "0.1-SNAPSHOT"

  val appDependencies = Seq(
    jdbc
  )

  def customLessEntryPoints(base: File): PathFinder = (
    (base / "app" / "assets" / "stylesheets" * "*.less")
    )

  val buildScalaVersion = "2.10.0"

  val main = play.Project(appName, appVersion, appDependencies).settings(
    lessEntryPoints <<= baseDirectory(customLessEntryPoints),
    resolvers := Seq(eligosource),
    libraryDependencies ++= List(
      "com.typesafe.akka" %% "akka-actor" % akka withSources(),
      "com.typesafe.akka" %% "akka-testkit" % akka withSources(),
      "org.eligosource" %% "eventsourced" % eventsourced withSources(),
      "org.scalatest" %% "scalatest" % scalatest % "test " withSources()
    )
  )
}

object Versions {

  lazy val akka = "2.1.0"
  lazy val eventsourced = "0.5-M1"
  lazy val scalatest = "1.9.1"
  lazy val squeryl = "0.9.5-4"
}

object Resolvers {

  lazy val eligosource = "Eligosource Releases Repo" at "http://repo.eligotech.com/nexus/content/repositories/eligosource-releases/"
}