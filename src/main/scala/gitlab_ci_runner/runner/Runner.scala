package gitlab_ci_runner.runner

import gitlab_ci_runner.helper.Network
import gitlab_ci_runner.conf.Config
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.collection.mutable.ListBuffer

object Runner extends App {
  val builds = ListBuffer.empty[Build]

  Config.loadConfig

  if (!Config.isConfigured)
    Setup.main(Array.empty)

  println("* GitLab CI runner started")
  println("* Waiting for builds")

  while (true) {
    Network.getBuild match {
      case Some(binfo) =>
        val b = new Build(binfo)
        Future {
          synchronized { builds += b }
          println(s"[${new java.util.Date().toString}] Build ${binfo.id} started...")
          try b.run()
          finally {
            println(s"[${new java.util.Date().toString}] Build ${binfo.id} ended")
            synchronized {
              builds -= b
            }
          }
        }
      case None =>
        Thread.sleep(30000)
    }
    println("Currrently running builds: " + builds.map(_.buildInfo).mkString("\n\t"))
  }
}
