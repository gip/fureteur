name := "fureteur"

organization := "com.entelo"

version := "0.0.1"

crossScalaVersions := Seq("2.9.1")

resolvers += "Twitter Repository" at "http://maven.twttr.com"

resolvers += "Akka Maven" at "http://akka.io/repository"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies <++= scalaVersion { scalaVersion =>
  Seq(
    "commons-pool"   % "commons-pool"  % "1.5.6",
    "org.slf4j"      % "slf4j-api"     % "1.6.1",
    "org.slf4j"      % "slf4j-log4j12" % "1.6.1"  % "provided",
    "log4j"          % "log4j"         % "1.2.16" % "provided",
    "junit"          % "junit"         % "4.8.1"  % "test",
    "org.apache.httpcomponents"    % "httpcore"     % "4.1",
    "org.apache.httpcomponents"    % "httpclient"  % "4.1",
    //"com.typesafe.akka" % "akka-actor" % "1.3.1"
    "se.scalablesolutions.akka" % "akka-actor" % "1.3.1"
  )
}

