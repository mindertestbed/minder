organization := "gov.tubitak.minder"

name := """minder"""

offline := true

version := "0.1.0-beta5"

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
  "org.beybunproject" % "xoola" % "1.0.0",
  "gov.tubitak.minder" % "minder-common" % "0.0.6",
  "gov.tubitak.minder" %% "minder-tdl" % "0.1.6",
  "com.feth" %%  "play-authenticate" % "0.6.5-SNAPSHOT",
  "be.objectify" %% "deadbolt-java" % "2.3.0-RC1",
  "org.webjars" %% "webjars-play" % "2.3.0",
  "org.webjars" % "bootstrap" % "3.2.0",
  "org.apache.commons" % "commons-email" % "1.3.1",
  "net.sf.saxon" % "Saxon-HE" % "9.6.0-3",
  "net.sf.jasperreports" % "jasperreports" % "6.0.0",
  "net.sourceforge.barbecue" % "barbecue" % "1.5-beta1",
  "org.codehaus.groovy" % "groovy" % "2.3.9",
  "gov.tubitak.minder.test" % "xml-value-initiator" % "0.0.1" % "test",
  "gov.tubitak.minder.test" % "xml-generator" % "0.0.1" % "test",
  "org.eclipse.aether" % "aether-api" % "1.0.2.v20150114",
  "org.eclipse.aether" % "aether-spi" % "1.0.2.v20150114",
  "org.eclipse.aether" % "aether-util" % "1.0.2.v20150114",
  "org.eclipse.aether" % "aether-impl" % "1.0.2.v20150114",
  "org.eclipse.aether" % "aether-connector-basic" % "1.0.2.v20150114",
  "org.eclipse.aether" % "aether-transport-classpath" % "1.0.2.v20150114",
  "org.eclipse.aether" % "aether-transport-file" % "1.0.2.v20150114",
  "org.eclipse.aether" % "aether-transport-http" % "1.0.2.v20150114",
  "org.eclipse.aether" % "aether-transport-wagon" % "1.0.2.v20150114",
  "org.apache.maven" % "maven-aether-provider" % "3.1.0"
)

resolvers ++= Seq(
  "Apache" at "http://repo1.maven.org/maven2/",
  "jBCrypt Repository" at "http://repo1.maven.org/maven2/org/",
  "play-easymail (release)" at "http://joscha.github.io/play-easymail/repo/releases/",
  "play-easymail (snapshot)" at "http://joscha.github.io/play-easymail/repo/snapshots/",
  Resolver.url("Objectify Play Repository", url("http://schaloner.github.io/releases/"))(Resolver.ivyStylePatterns),
  "play-authenticate (release)" at "http://joscha.github.io/play-authenticate/repo/releases/",
  "play-authenticate (snapshot)" at "http://joscha.github.io/play-authenticate/repo/snapshots/",
  "jasper" at "http://jaspersoft.artifactoryonline.com/jaspersoft/third-party-ce-artifacts/"
)
