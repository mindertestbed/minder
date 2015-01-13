organization := "gov.tubitak.minder"

name := "minder"

version := "0.0.4"

resolvers += "play-authenticate (snapshot)" at "http://joscha.github.io/play-authenticate/repo/snapshots/"

resolvers += "play-easymail (snapshot)" at "http://joscha.github.io/play-easymail/repo/snapshots/"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

resolvers += "Eid public repository" at "http://eidrepo:8081/nexus/content/groups/public/"

resolvers += Resolver.mavenLocal

scalaVersion := "2.11.2"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  javaCore,
  javaJdbc,
  javaEbean,
  "org.scala-lang" % "scala-library" % "2.11.4",
  "org.scala-lang" % "scala-reflect" % "2.11.4",
  "org.scala-lang" % "scala-actors" % "2.11.4",
  "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.2",
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
  "org.beybunproject" % "xoola" % "1.0.0-RC3",
  "gov.tubitak.minder" % "minder-common" % "0.0.5",
  "gov.tubitak.minder" %% "minder-tdl" % "0.0.8",
  "com.feth" %%  "play-authenticate" % "0.6.5-SNAPSHOT",
  "be.objectify" %% "deadbolt-java" % "2.3.0-RC1",
  "org.webjars" %% "webjars-play" % "2.3.0",
  "org.webjars" % "bootstrap" % "3.2.0",
  "org.apache.commons" % "commons-email" % "1.3.1",
  "net.sf.saxon" % "Saxon-HE" % "9.6.0-3",
  "gov.tubitak.minder.test" % "xml-value-initiator" % "0.0.1" % "test",
  "gov.tubitak.minder.test" % "xml-generator" % "0.0.1" % "test"
)

resolvers ++= Seq(
  "Apache" at "http://repo1.maven.org/maven2/",
  "jBCrypt Repository" at "http://repo1.maven.org/maven2/org/",
  "play-easymail (release)" at "http://joscha.github.io/play-easymail/repo/releases/",
  "play-easymail (snapshot)" at "http://joscha.github.io/play-easymail/repo/snapshots/",
  Resolver.url("Objectify Play Repository", url("http://schaloner.github.io/releases/"))(Resolver.ivyStylePatterns),
  "play-authenticate (release)" at "http://joscha.github.io/play-authenticate/repo/releases/",
  "play-authenticate (snapshot)" at "http://joscha.github.io/play-authenticate/repo/snapshots/"
)
