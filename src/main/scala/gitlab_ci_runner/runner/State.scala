package gitlab_ci_runner.runner

sealed trait State

object State {
  case object Running extends State
  case object Failed extends State
  case object Success extends State
  case object Waiting extends State
}
