organization := "gov.tubitak.minder"

name := """minder"""

version := "1.7-Pre1"

lazy val minder = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

products in Compile <<= products in Aspectj

resolvers += "Eid public repository" at "http://193.140.74.199:8081/nexus/content/groups/public/"

resolvers += Resolver.mavenLocal

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  javaJdbc,
  evolutions,
  "xalan" % "serializer" % "2.7.2",
  "com.typesafe.play" % "play-java-ws_2.11" % "2.4.2",
  "com.typesafe.play" % "play-cache_2.11" % "2.4.2",
  "com.typesafe.play" % "play-java_2.11" % "2.4.2",
  "org.scala-lang" % "scala-library" % "2.11.7",
  "org.scala-lang" % "scala-reflect" % "2.11.7",
  "org.scala-lang" % "scala-actors" % "2.11.7",
  "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.5",
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
  "mysql" % "mysql-connector-java" % "5.1.18",
  "org.beybunproject" % "xoola" % "1.2.2",
  "gov.tubitak.minder" % "minder-common" % "0.4.4",
  "gov.tubitak.minder" %% "minder-tdl" % "0.6",
  "org.webjars" % "webjars-play_2.11" % "2.4.0-1",
  "org.webjars" % "bootstrap" % "3.3.6",
  "org.codehaus.groovy" % "groovy" % "2.3.9",
  "gov.tubitak.minder" % "minder-gitb-bridge-common" % "0.0.1",
  "com.itextpdf" % "itextpdf" % "5.5.8",
  "com.itextpdf.tool" % "xmlworker" % "5.5.8",
  specs2 % Test,
  "junit" % "junit" % "4.12" % Test
).map(_.excludeAll(
  ExclusionRule(organization = "bouncycastle"),
  ExclusionRule(organization = "org.bouncycastle"),
  ExclusionRule(organization = "org.jboss.logging"),
  ExclusionRule(organization = "org.apache.cxf"),
  ExclusionRule(organization = "com.h2database", name="h2-1.4.187.jar"),
  ExclusionRule(organization = "commons-logging")))

resolvers ++= Seq(
  "Apache" at "http://repo1.maven.org/maven2/",
  "jBCrypt Repository" at "http://repo1.maven.org/maven2/org/",
  "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
  "Objectify Play Repository" at "http://schaloner.github.io/releases/",
  "play-authenticate (release)" at "http://joscha.github.io/play-authenticate/repo/releases/",
  "jasper" at "http://jaspersoft.artifactoryonline.com/jaspersoft/third-party-ce-artifacts/"
)

includeFilter in(Assets, LessKeys.less) := "ui2.less" | "oldui.less"

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
