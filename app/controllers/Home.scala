package controllers

import _root_.helpers.ActorUtils.withActor
import _root_.helpers.LoggedActor
import helpers.AsyncAction
import helpers.AsyncAction._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import akka.actor._
import concurrent.{Future, Promise}
import services.actors._
import services.actors.AuthorizationSuccess
import services.actors.AuthorizationCommand
import services.actors.AuthorizationFailure

object Home extends Controller {

  import play.api.Play.current
  import play.api.libs.concurrent.Akka
  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  def withAuthenticationActor =
    withActor(actor => Akka.system.actorOf(Props(new AuthenticationActor(actor) with DefaultActorProvider with HasContext with LoggedActor))) _

  val provider = new DefaultActorProvider with HasContext {
    def context = Akka.system
  }

  val services: ActorRef = provider.createActor(classOf[ServicesActor])

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

  def formValid(token: UsernamePasswordToken): Future[Result] = withAuthenticationActor(AuthorizationCommand(token)) collect {
    case AuthorizationSuccess(userCredentials) =>
      authorizationSuccess(token, userCredentials)
    case AuthorizationFailure() =>
      authorizationFailure(token)
  }

  def authorizationSuccess(token: UsernamePasswordToken, userCredentials: UserCredentials) =
    loginView(userForm.fill(token)).withSession("sessionKey" -> userCredentials.sessionKey)

  def authorizationFailure(token: UsernamePasswordToken) =
    loginView(userForm.fill(token).withGlobalError("User invalid"))

  private def loginView(form: Form[UsernamePasswordToken]) =
    Ok(views.html.home.form(form))
}