package gitlab_ci_runner.runner

import gitlab_ci_runner.conf.Config
import gitlab_ci_runner.helper.Network
import gitlab_ci_runner.helper.SSHKey
import scala.util.Properties

object Setup extends App {
  SSHKey.getPublicKey match {
    case None =>
      Console.err.println(s"No public key found. Please generate an SSH key pair in ${Properties.userHome}/.ssh/ida_rsa.pub before running.")
    case Some(key) =>
      args.toSeq match {
        case Seq(url, token) =>
          Config.url = url
          registerRunner(token)
        case Seq() =>
          println("Please provide the following info:")
          println()

          def getUrl: String =
            readLine("Please enter the gitlab-ci coordinator URL (e.g. http://gitlab-ci.org:3000/ ) ") match {
              case null | "" => getUrl
              case s         => s
            }

          Config.url = getUrl

          println()

          def getToken: String =
            readLine("Please enter the gitlab-ci token for this runner: ") match {
              case null | "" => getToken
              case s         => s
            }

          registerRunner(getToken)
        case _ =>
          Console.err.println("Usage:")
          Console.err.println("  Interactive setup:")
          Console.err.println("    java -cp <runner.jar> gitlab_ci_runner.runner.Setup")
          Console.err.println("  Noninteractive setup:")
          Console.err.println("    java -cp <runner.jar> gitlab_ci_runner.runner.Setup <url> <token>")
      }

      def registerRunner(token: String) = {
        Network.registerRunner(key, token) match {
          case Some(tok) =>
            Config.token = tok
            Config.saveConfig
            println()
            println("Runner registered successfully. Feel free to start it!")
          case None =>
            println()
            Console.err.println("Failed to register this runner. Perhaps your SSH key is invalid or you are having network problems")
        }
      }
  }
}
