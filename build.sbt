name := """minder"""

version := "1.0-SNAPSHOT"

resolvers += "Eid public repository" at "http://eidrepo:8081/nexus/content/groups/public/"

resolvers += Resolver.mavenLocal

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "org.apache.derby" % "derby" % "10.4.1.3",
  "org.scala-lang" % "scala-library" % "2.11.4",
  "org.scala-lang" % "scala-reflect" % "2.11.4",
  "org.scala-lang" % "scala-actors" % "2.11.4",
  "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.2",
  "com.typesafe.slick" % "slick_2.11" % "2.1.0",
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
  "org.beybunproject" % "xoola" % "1.0.0-RC1"
)
