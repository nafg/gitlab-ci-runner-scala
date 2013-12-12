package gitlab_ci_runner.conf

import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.Properties

object Config {
  var url = ""
  var token = ""
    
  val dir = new File(System.getProperty("user.home"), ".gitlab-ci-runner-scala")

  val configFile = new File(dir, "config.properties")
  
  def loadConfig() = if(configFile.exists) {
    val props = new Properties
    props.load(new FileReader(configFile))
    url = props.getProperty("url")
    token = props.getProperty("token")
  }
  
  def saveConfig = {
    configFile.getParentFile.mkdirs()
    val props = new Properties
    props.setProperty("url", url)
    props.setProperty("token", token)
    props.store(new FileWriter(configFile), null)
  }
  
  def isConfigured = !url.isEmpty && !token.isEmpty
}
