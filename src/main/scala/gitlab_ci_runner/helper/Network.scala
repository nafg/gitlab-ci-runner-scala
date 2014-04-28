package gitlab_ci_runner
package helper

import java.net.URL
import scala.util.parsing.json.JSON
import scala.util.parsing.json.JSONObject
import gitlab_ci_runner.conf.Config
import gitlab_ci_runner.helper.json.BuildInfo
import gitlab_ci_runner.runner.State
import uk.co.bigbeeconsultants.http.HttpClient
import uk.co.bigbeeconsultants.http.header.MediaType
import uk.co.bigbeeconsultants.http.request.Request
import uk.co.bigbeeconsultants.http.request.RequestBody
import scala.util.Try

object Network {
  JSON.globalNumberParser = _.toInt

  private def request(url: String, content: Map[String, String] = Map.empty)(makeReq: (URL, RequestBody) => Request) = {
    val httpClient = new HttpClient
    val req = makeReq(new URL(url), RequestBody(content, MediaType.APPLICATION_FORM_URLENCODED))
    val resp = httpClient.makeRequest(req)
    if (resp.status.isSuccess)
      Some(resp.body.asString)
    else
      None
  }

  private def put(url: String, content: Map[String, String]): Option[String] = try {
    request(url, content)((url, body) => Request.put(url, body))
  } catch {
    case e: Exception => None
  }

  private def post(url: String, content: Map[String, String], tries: Int = 5): Option[String] =
    if (tries <= 0) None else try {
      request(url, content)((url, body) => Request.post(url, Some(body)))
    } catch {
      case e: Exception =>
        println(e)
        Thread.sleep(1000)
        post(url, content, tries - 1)
    }

  private def apiUrl = Config.url + (if (Config.url.endsWith("/")) "" else "/") + "api/v1"

  def registerRunner(pubKey: String, token: String): Option[String] = {
    val postBody = Map("token" -> token, "public_key" -> pubKey)
    post(apiUrl + "/runners/register.json", postBody) flatMap JSON.parseRaw flatMap {
      case JSONObject(resp) =>
        try {
          Some(resp("token").toString)
        } catch {
          case e: Exception => None
        }
      case _ => None
    }
  }

  def getBuild: Option[BuildInfo] = {
    val postBody = Map("token" -> Config.token)
    post(apiUrl + "/builds/register.json", postBody) flatMap {
      case "" =>
        println("Nothing")
        None
      case resp =>
        try {
          JSON.parseRaw(resp) flatMap {
            case JSONObject(obj) =>
              if (obj.keySet == Set("message")) {
                println("ERROR: " + obj("message"))
                None
              } else if (Set("id", "project_id", "commands", "repo_url", "sha", "ref") subsetOf obj.keySet) {
                val timeout = scala.util.Try {
                  request(apiUrl + "/projects/" + obj("id").toString)((url, _) => Request.get(url)) flatMap { resp =>
                    JSON.parseRaw(resp) flatMap {
                      case JSONObject(obj) => Some(obj("timeout").toString.toInt)
                      case _               => None
                    }
                  }
                }.toOption.flatten getOrElse 7200
                Some(BuildInfo(
                  id = obj("id").toString.toInt,
                  projectId = obj("project_id").toString.toInt,
                  commands = obj("commands").toString,
                  repoUrl = obj("repo_url").toString,
                  reference = obj("sha").toString,
                  refName = obj("ref").toString,
                  timeout = timeout
                ))
              } else {
                println("Unrecognized response: " + obj)
                None
              }
            case _ =>
              println("Invalid JSON: " + resp)
              None
          }
        } catch {
          case e: Exception =>
            println(e)
            println("Failed")
            None
        }
    }
  }

  def pushBuild(id: Int, state: State, trace: String) = {
    println(s"[${new java.util.Date().toString}] Submitting build $id to coordinator at state $state...")
    val stateStr = state match {
      case State.Running => "running"
      case State.Success => "success"
      case State.Failed  => "failed"
      case State.Waiting => "waiting"
    }
    val putBody = Map("token" -> Config.token, "state" -> stateStr, "trace" -> trace)
    def attempt(tries: Int): Boolean = if (tries == 0) false else {
      val putted = Try {
        put(s"$apiUrl/builds/$id.json", putBody).isDefined
      }.getOrElse(false)
      putted || {
        Console.err.println("Pushing build state failed, trying again $tries times")
        Thread.sleep(1000)
        attempt(tries - 1)
      }
    }
    attempt(5)
  }
}
