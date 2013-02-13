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
    resolvers := Seq(`eligosource-releases`, `eligosource-snapshots`),
    libraryDependencies ++= List(
      "com.typesafe.akka" %% "akka-actor" % akka withSources(),
      "com.typesafe.akka" %% "akka-testkit" % akka withSources(),
      "org.eligosource" %% "eventsourced-core" % eventsourced withSources(),
      "org.eligosource" %% "eventsourced-journal-journalio" % eventsourced withSources(),
      "org.scalaj" % "scalaj-time_2.10.0-M7" % `scalaj-time`,
      "org.scalatest" %% "scalatest" % scalatest % "test " withSources()
    )
  )
}

object Versions {

  lazy val akka = "2.1.0"
  lazy val eventsourced = "0.5-SNAPSHOT"
  lazy val scalatest = "1.9.1"
  lazy val `scalaj-time` = "0.6"
  lazy val squeryl = "0.9.5-4"
}

object Resolvers {

  lazy val `eligosource-releases` =
    "Eligosource Releases" at "http://repo.eligotech.com/nexus/content/repositories/eligosource-releases"
  lazy val `eligosource-snapshots` =
    "Eligosource Snapshots" at "http://repo.eligotech.com/nexus/content/repositories/eligosource-snapshots"
}