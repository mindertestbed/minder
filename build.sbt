organization := "com.yerlibilgin.minder"

name := """minder"""

version := "2.2.8"

lazy val minder = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

products in Compile <<= products in Aspectj

resolvers += Resolver.mavenLocal

fork := true

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  javaJdbc,
  evolutions,
  "xalan" % "serializer" % "2.7.2",
  "com.typesafe.play" % "play-java-ws_2.11" % "2.5.8",
  "com.typesafe.play" % "play-cache_2.11" % "2.5.8",
  "com.typesafe.play" % "play-java_2.11" % "2.5.8",
  "org.scala-lang" % "scala-library" % "2.11.7",
  "org.scala-lang" % "scala-reflect" % "2.11.7",
  "org.scala-lang" % "scala-actors" % "2.11.7",
  "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.5",
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "org.beybunproject" % "xoola" % "1.3.0",
  "com.yerlibilgin.minder" % "minder-common" % "1.1.0",
  "com.yerlibilgin.minder" %% "minder-tdl" % "1.1.0",
  "org.webjars" % "webjars-play_2.11" % "2.4.0-1",
  "org.webjars" % "bootstrap" % "3.3.6",
  //"org.codehaus.groovy" % "groovy" % "2.3.9",
  "gov.tubitak.minder" % "minder-gitb-bridge-common" % "0.1",
  "com.itextpdf" % "itextpdf" % "5.5.13",
  "com.itextpdf.tool" % "xmlworker" % "5.5.13",
  specs2 % Test,
  "junit" % "junit" % "4.12" % Test
).map(_.excludeAll(
  ExclusionRule(organization = "bouncycastle"),
  ExclusionRule(organization = "org.bouncycastle"),
  ExclusionRule(organization = "org.jboss.logging"),
  ExclusionRule(organization = "org.apache.cxf"),
  ExclusionRule(organization = "commons-logging")))

resolvers ++= Seq(
  "Apache" at "http://repo1.maven.org/maven2/",
  "jBCrypt Repository" at "http://repo1.maven.org/maven2/org/",
  "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
  "Objectify Play Repository" at "http://schaloner.github.io/releases/",
  "play-authenticate (release)" at "http://joscha.github.io/play-authenticate/repo/releases/",
  "jasper" at "http://jaspersoft.artifactoryonline.com/jaspersoft/third-party-ce-artifacts/"
)

import com.typesafe.sbt.SbtAspectj.AspectjKeys.{binaries, inputs}
import com.typesafe.sbt.SbtAspectj.{Aspectj, aspectjSettings, compiledClasses}

aspectjSettings

inputs in Aspectj <+= compiledClasses

binaries in Aspectj <++= update map { report =>
  report.matching(
    moduleFilter(organization = "org.springframework", name = "spring-aspects")
  )
}

products in Compile <<= products in Aspectj

products in Runtime <<= products in Compile

routesGenerator := InjectedRoutesGenerator
