package controllers

import support.AsyncAction
import support.AsyncAction._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import concurrent.{Future, Promise}
import services.actors._
import services.actors.AuthorizationSuccess
import services.actors.AuthorizationCommand
import services.actors.AuthorizationFailure

import akka.pattern.ask
import concurrent.duration._
import services.actors.support.{DefaultActorProvider, HasContext}

object Home extends Controller {

  import play.api.Play.current
  import play.api.libs.concurrent.Akka
  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  val provider = new DefaultActorProvider with HasContext {
    def context = Akka.system
  }

  provider.createActor[ServicesActor]

  private val userForm = Form(
    mapping(
      "username" -> text,
      "password" -> text
    )(UsernamePasswordToken.apply)(UsernamePasswordToken.unapply)
  )

  def form = Action {
    loginView(userForm)
  }

  def submit = AsyncAction {
    implicit request =>
      userForm.bindFromRequest.fold(formInvalid, formValid)
  }

  private def formInvalid(form: Form[UsernamePasswordToken]) =
    Promise.successful(loginView(form)).future

  def formValid(token: UsernamePasswordToken): Future[Result] = {
    implicit val timeout = akka.util.Timeout(5 seconds)
    provider.actorFor[AuthenticationActor] ? AuthorizationCommand(token) collect {
      case AuthorizationSuccess(userCredentials) =>
        authorizationSuccess(token, userCredentials)
      case AuthorizationFailure() =>
        authorizationFailure(token)
    }
  }

  def authorizationSuccess(token: UsernamePasswordToken, userCredentials: UserCredentials) =
    loginView(userForm.fill(token)).withSession("sessionKey" -> userCredentials.sessionKey)

  def authorizationFailure(token: UsernamePasswordToken) =
    loginView(userForm.fill(token).withGlobalError("User invalid"))

  private def loginView(form: Form[UsernamePasswordToken]) =
    Ok(views.html.home.form(form))
}