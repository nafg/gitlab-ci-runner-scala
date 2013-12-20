gitlab-ci-runner-scala
======================

A runner for GitLab CI written in Scala (based on https://github.com/virtualmarc/gitlab-ci-runner-win)

Because it runs on the JVM, it's platform agnostic.

Note: It does not generate SSH keys for you. It expects to find the public key in $HOME/.ssh/id_rsa.pub.

Usage:
 - Download the latest release from [https://github.com/nafg/gitlab-ci-runner-scala/releases](the GitHub releases page), e.g., [https://github.com/nafg/gitlab-ci-runner-scala/releases/download/v0.1/gitlabci-runner.jar](v0.1).
 - Run `java -cp /path/to/gitlabci-runner.jar gitlab_ci_runner.runner.Setup`
   (you can enter the url and token interactively, or pass them on the command line in that order).
 - Run `java -jar /path/to/gitlabci-runner.jar` to start the runner.

 
