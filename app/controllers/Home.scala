package controllers

import helpers.AsyncAction
import helpers.AsyncAction._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import akka.actor.{TypedProps, TypedActor}
import services.{AuthenticationServiceImpl, AuthenticationService, UsernamePasswordToken}
import concurrent.{Promise, Future}
import controllers.helpers.FormHelpers.toExtendedForm

object Home extends Controller {

  import play.api.Play.current
  import play.api.libs.concurrent.Akka
  import play.api.libs.concurrent.Execution.Implicits._

  private val authenticationService: AuthenticationService =
    TypedActor(Akka.system).typedActorOf(TypedProps[AuthenticationServiceImpl]())

  private val userForm = Form(
    mapping(
      "username" -> text,
      "password" -> text
    )(UsernamePasswordToken.apply)(UsernamePasswordToken.unapply)
  )

  def form = Action {
    formView(userForm)
  }

  def submit = AsyncAction {
    implicit request =>
      userForm.bindFromRequest.addValidation(userAuthenticated).flatMap {
        form => form.fold(userNotFound, userFound)
      }
  }

  private def userAuthenticated(form: Form[UsernamePasswordToken]) =
    authenticationService.authenticate(form.get).map {
      isAuthenticated =>
        if (isAuthenticated)
          form
        else
          form.withGlobalError("User invalid")
    }

  private def userNotFound(form: Form[UsernamePasswordToken]): Future[Result] =
    Promise.successful(Ok(views.html.home.form(form))).future

  private def userFound(token: UsernamePasswordToken)(implicit request: Request[_]): Future[Result] =
    authenticationService.authorize(token) map {
      userCredentials =>
        formView(userForm.fill(token)).withSession(
          "sessionKey" -> userCredentials.sessionKey
        )
    }

  def formView(form: Form[UsernamePasswordToken]): Result =
    Ok(views.html.home.form(form))
}