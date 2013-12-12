package gitlab_ci_runner.runner

import gitlab_ci_runner.conf.Config
import gitlab_ci_runner.helper.Network
import gitlab_ci_runner.helper.SSHKey

object Setup extends App {
  args.toSeq match {
    case Seq(url, token) =>
      Config.url = url
      registerRunner(token)
    case Seq() =>
      println("This seems to be the first run,")
      println("please provide the following info to proceed:")
      println()

      def getUrl: String =
        readLine("Please enter the gitlab-ci coordinator URL (e.g. http://gitlab-ci.org:3000/ ) ") match {
          case null | "" => getUrl
          case s => s
        }

      Config.url = getUrl

      println()

      def getToken: String =
        readLine("Please enter the gitlab-ci token for this runner: ") match {
          case null | "" => getToken
          case s => s
        }

      registerRunner(getToken)
    case _ =>
      println("Usage:")
      println("  Interactive setup:")
      println("    java -cp <runner.jar> gitlab_ci_runner.runner.Setup")
      println("  Noninteractive setup:")
      println("    java -cp <runner.jar> gitlab_ci_runner.runner.Setup <url> <token>")
  }

  private def registerRunner(token: String) = {
    SSHKey.getPublicKey.flatMap(key => Network.registerRunner(key, token)) match {
      case Some(tok) =>
        Config.token = tok
        Config.saveConfig
        println()
        println("Runner registered successfully. Feel free to start it!")
      case None =>
        println()
        println("Failed to register this runner. Perhaps your SSH key is invalid or you are having network problems")
    }
  }
}
