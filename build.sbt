import AssemblyKeys._

libraryDependencies ++= Seq(
    "uk.co.bigbeeconsultants" %% "bee-client" % "0.21.+",
    "org.slf4j" % "slf4j-api" % "1.7.+",
    "ch.qos.logback" % "logback-core"    % "1.0.+",
    "ch.qos.logback" % "logback-classic" % "1.0.+"
)

resolvers += "Big Bee Consultants" at "http://repo.bigbeeconsultants.co.uk/repo"

scalaVersion := "2.10.3"

mainClass := Some("gitlab_ci_runner.runner.Runner")

assemblySettings

jarName in assembly := "gitlabci-runner.jar"
