package gitlab_ci_runner.helper

import java.io.File
import scala.util.Properties
import scala.io.Source

object SSHKey {
  def getPublicKey = {
    val file = new File(new File(Properties.userHome, ".ssh"), "id_rsa.pub")
    if (file.exists) Some(Source.fromFile(file).getLines.mkString.trim)
    else None
  }
}
