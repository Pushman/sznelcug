import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

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
    libraryDependencies ++= List(
      "org.slf4j" % "slf4j-nop" % "1.6.4",
      "com.h2database" % "h2" % "1.3.166",
      "org.squeryl" % "squeryl_2.10.0-RC1" % "0.9.5-4"
    )
  )
}