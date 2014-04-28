package gitlab_ci_runner.helper.json

case class BuildInfo(id: Int, projectId: Int, commands: String, repoUrl: String, reference: String, refName: String, timeout: Int)
