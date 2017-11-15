name := "voting-board"

version := "0.1"

scalaVersion := "2.12.4"

resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies += "org.scorexfoundation" %% "scrypto" % "2.0.3"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % "test"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")