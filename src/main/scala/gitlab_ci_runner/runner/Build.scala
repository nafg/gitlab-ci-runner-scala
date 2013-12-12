package gitlab_ci_runner.runner

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

import scala.collection.mutable.ListBuffer

import gitlab_ci_runner.conf.Config
import gitlab_ci_runner.helper.json.BuildInfo

class Build(val buildInfo: BuildInfo) {

  private var _completed = false

  def completed = _completed

  private val output = ListBuffer.empty[String]

  def outputStr = output.map(_ + "\n").mkString

  private val projectsDir = new File(Config.dir, "projects")

  private var projectDir: File = new File(projectsDir, "project-" + buildInfo.projectId)

  var state: State = State.Waiting

  var timeout = 7200

  def run = {
    state = State.Running

    projectsDir.mkdirs()

    val gitCmd = if (new File(projectDir, ".git").exists)
      fetchCmd + "\n" + checkoutCmd
    else
      cloneCmd

    val commands = gitCmd + "\n" + buildInfo.commands

    val scriptFile = File.createTempFile(s"build-${ buildInfo.id }-script", ".sh", projectsDir)

    val wr = new java.io.FileWriter(scriptFile)
    wr.write(commands)
    wr.flush


    if (exec(scriptFile))
      state = State.Success
    else
      state = State.Failed

    _completed = true
  }

  def exec(script: File) = try {
    val p = new ProcessBuilder("sh", "-x", "-e", script.getAbsolutePath)

    p.directory(projectsDir)

    p.redirectErrorStream(true)

    val env = Map(
      "CI_SERVER" -> "yes",
      "CI_BUILD_REF" -> buildInfo.reference,
      "CI_BUILD_REF_NAME" -> buildInfo.refName,
      "CI_BUILD_ID" -> buildInfo.id.toString)

    env foreach { case (k, v) => p.environment().put(k, v) }

    val proc = p.start()
    val startTime = System.currentTimeMillis()
    val out = new BufferedReader(new InputStreamReader(proc.getInputStream()))
    def loop: Int = {
      if (System.currentTimeMillis() - startTime >= timeout * 1000)
        proc.destroy()
      out.readLine() match {
        case null =>
          proc.waitFor()
          proc.exitValue()
        case line =>
          println(line)
          output += line
          loop
      }
    }
    loop == 0
  } catch {
    case e: Exception =>
      println(e)
      false
  }

  def checkoutCmd =
    "cd " + projectDir.getAbsolutePath() +
      " && git reset --hard && git checkout " + buildInfo.reference

  def cloneCmd =
    "cd " + projectsDir.getAbsolutePath() +
      " && git clone " + buildInfo.repoUrl + " project-" + buildInfo.projectId +
      " && cd " + projectDir.getAbsolutePath() +
      " && git checkout " + buildInfo.reference

  def fetchCmd =
    "cd " + projectDir.getAbsolutePath() +
      " && git reset --hard" +
      " && git clean -f" +
      " && git fetch"
}
