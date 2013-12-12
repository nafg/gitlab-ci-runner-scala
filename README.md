gitlab-ci-runner-scala
======================

A runner for GitLab CI written in Scala (based on https://github.com/virtualmarc/gitlab-ci-runner-win)

Because it runs on the JVM, it's platform agnostic.

Note: It does not generate SSH keys for you. It expects to find the public key in $HOME/.ssh/id_rsa.pub.

Usage:
 - Run `sbt package` to create the jar file
 - Run something to the effect of
   `java -cp ~/.ivy2/cache/org.scala-lang/scala-library/jars/scala-library-2.10.3.jar:target/scala-2.10/gitlab-ci-scala_2.10-0.1-SNAPSHOT.jar gitlab_ci_runner.runner.Setup`
   (you can enter the url and token interactively, or pass them on the command line in that order).
   
 - Run it as `java -cp ~/.ivy2/cache/org.scala-lang/scala-library/jars/scala-library-2.10.3.jar:target/scala-2.10/gitlab-ci-scala_2.10-0.1-SNAPSHOT.jar gitlab_ci_runner.runner.Runner`
 
