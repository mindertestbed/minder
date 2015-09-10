organization := "gov.tubitak.minder"

name := """minder"""

version := "1.2.0-beta4"

lazy val minder = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

products in Compile <<= products in Aspectj

resolvers += "Eid public repository" at "http://193.140.74.199:8081/nexus/content/groups/public/"

resolvers += Resolver.mavenLocal

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  javaJdbc,
  specs2 % Test,
  evolutions,
  "com.typesafe.play" % "play-java-ws_2.11" % "2.4.2",
  "com.typesafe.play" % "play-cache_2.11" % "2.4.2",
  "com.typesafe.play" % "play-java_2.11" % "2.4.2",
  "com.typesafe.play" % "twirl-api_2.11" % "1.1.1",
  "org.scala-lang" % "scala-library" % "2.11.7",
  "org.scala-lang" % "scala-reflect" % "2.11.7",
  "org.scala-lang" % "scala-actors" % "2.11.7",
  "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.5",
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
  "mysql" % "mysql-connector-java" % "5.1.18",
  "org.beybunproject" % "xoola" % "1.0.1",
  "gov.tubitak.minder" % "minder-common" % "0.3.1",
  "gov.tubitak.minder" %% "minder-tdl" % "0.3.6-beta2",
  "org.webjars" % "webjars-play_2.11" % "2.4.0-1",
  "org.webjars" % "bootstrap" % "3.3.5",
  "net.sf.saxon" % "Saxon-HE" % "9.6.0-3",
  "net.sf.jasperreports" % "jasperreports" % "6.0.0",
  "net.sourceforge.barbecue" % "barbecue" % "1.5-beta1",
  "org.codehaus.groovy" % "groovy" % "2.3.9"
)

resolvers ++= Seq(
  "Apache" at "http://repo1.maven.org/maven2/",
  "jBCrypt Repository" at "http://repo1.maven.org/maven2/org/",
  "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
  "play-easymail (release)" at "http://joscha.github.io/play-easymail/repo/releases/",
  "Objectify Play Repository" at "http://schaloner.github.io/releases/",
  "play-authenticate (release)" at "http://joscha.github.io/play-authenticate/repo/releases/",
  "jasper" at "http://jaspersoft.artifactoryonline.com/jaspersoft/third-party-ce-artifacts/"
)

import com.typesafe.sbt.SbtAspectj.{ Aspectj, aspectjSettings, compiledClasses }
import com.typesafe.sbt.SbtAspectj.AspectjKeys.{ binaries, inputs, lintProperties }

aspectjSettings

inputs in Aspectj <+= compiledClasses

binaries in Aspectj <++= update map { report =>
  report.matching(
    moduleFilter(organization = "org.springframework", name = "spring-aspects")
  )
}

products in Compile <<= products in Aspectj

products in Runtime <<= products in Compile
