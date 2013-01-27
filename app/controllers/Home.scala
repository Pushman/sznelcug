package controllers

import helpers.AsyncAction
import helpers.AsyncAction._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import akka.actor._
import akka.pattern.ask
import concurrent.{Future, Promise}
import concurrent.duration._
import services.actors._
import services.UsernamePasswordToken
import services.actors.AuthorizationSuccess
import services.actors.AuthorizationCommand
import services.actors.AuthorizationFailure
import akka.util.Timeout

object Home extends Controller {

  import play.api.Play.current
  import play.api.libs.concurrent.Akka
  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  private val usersReadActor = Akka.system.actorOf(Props[UsersReadModelActor]())
  private val usersWriteActor = Akka.system.actorOf(Props[UsersWriteModelActor]())

  private val userForm = Form(
    mapping(
      "username" -> text,
      "password" -> text
    )(UsernamePasswordToken.apply)(UsernamePasswordToken.unapply)
  )

  def form = Action {
    loginFormView(userForm)
  }

  def submit = AsyncAction {
    implicit request =>
      userForm.bindFromRequest.fold(
        userNotFound,
        formValid
      )
  }

  def formValid(token: UsernamePasswordToken): Future[Result] = {
    implicit val timeout = Timeout(5 seconds)
    Akka.system.actorOf(Props(new AuthenticationActor(usersReadActor, usersWriteActor))) ? AuthorizationCommand(token) collect {
      case AuthorizationFailure() =>
        loginFormView(userForm.fill(token).withGlobalError("User invalid"))
      case AuthorizationSuccess(userCredentials) =>
        loginFormView(userForm.fill(token)).withSession("sessionKey" -> userCredentials.sessionKey)
    }
  }

  private def userNotFound(form: Form[UsernamePasswordToken]) =
    Promise.successful(loginFormView(form)).future

  private def loginFormView(form: Form[UsernamePasswordToken]) =
    Ok(views.html.home.form(form))
}