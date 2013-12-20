package gitlab_ci_runner.runner

import gitlab_ci_runner.helper.Network
import gitlab_ci_runner.conf.Config

object Runner extends App {
  var build: Build = _
  
  Config.loadConfig

  println("* GitLab CI runner started")
  println("* Waiting for builds")
  waitForBuild()

  def completed = running && build.completed

  def running = Option(build).isDefined

  def waitForBuild(): Unit = {
    if (running)
      updateBuild()
    else
      getBuild()
    Thread.sleep(30000)
    waitForBuild()
  }

  def updateBuild() = {
    if (build.completed) {
      if (pushBuild()) {
        println(s"[${new java.util.Date().toString}] Completed build ${build.buildInfo.id}")
        build = null
      }
    } else
      pushBuild()
  }

  def pushBuild() = Network.pushBuild(build.buildInfo.id, build.state, build.outputStr)

  def getBuild() = Network.getBuild foreach { binfo =>
    build = new Build(binfo)
    println(s"[${new java.util.Date().toString}] Build ${binfo.id} started...")
    new Thread {
      override def run = build.run
    }.start
  }
}
