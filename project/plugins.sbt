// Comment to get more information during initialization
logLevel := Level.Debug

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("play" % "sbt-plugin" % "2.1-RC2", "0.12", "2.9.2")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.2.0", "0.12", "2.9.2")