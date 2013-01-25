package controllers

import helpers.AsyncAction
import helpers.AsyncAction._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import akka.actor.{TypedProps, TypedActor}
import services.{UserCredentials, AuthenticationServiceImpl, AuthenticationService, UsernamePasswordToken}
import concurrent.Promise
import controllers.helpers.FormHelpers.toExtendedForm
import db.gateways.UsersGateway
import db.gateways.impl.UsersGatewayImpl
import db.gateways.helpers.FetchAsync

object Home extends Controller {

  import play.api.Play.current
  import play.api.libs.concurrent.Akka
  import play.api.libs.concurrent.Execution.Implicits.defaultContext

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
      for {
        validatedForm <- userForm.bindFromRequest.addValidation(userAuthenticated)
        result <- validatedForm.fold(userNotFound, userFound)
      } yield result
  }

  private def userAuthenticated(form: Form[UsernamePasswordToken]) =
    for {
      isAuthenticated <- authenticationService.authenticate(form.get)
    } yield if (isAuthenticated) form else form.withGlobalError("User invalid")

  private def userNotFound(form: Form[UsernamePasswordToken]) =
    Promise.successful(loginFormView(form)).future

  private def userFound(token: UsernamePasswordToken)(implicit request: Request[_]) =
    for {
      userCredentials <- authenticationService.authorize(token)
      _ <- updateUserSessionKey(token, userCredentials)
    } yield loginFormView(userForm.fill(token)).withSession("sessionKey" -> userCredentials.sessionKey)

  private val authenticationService: AuthenticationService =
    TypedActor(Akka.system).typedActorOf(TypedProps[AuthenticationServiceImpl]())

  private def updateUserSessionKey(token: UsernamePasswordToken, userCredentials: UserCredentials) = for {
    userOption <- usersGateway.findBy(username = token.username)
    userUpdate <- usersGateway.update(userOption.get.update(sessionKey = userCredentials.sessionKey))
  } yield FetchAsync(userUpdate)

  private val usersGateway: UsersGateway = TypedActor(Akka.system).typedActorOf(TypedProps[UsersGatewayImpl]())

  def loginFormView(form: Form[UsernamePasswordToken]) =
    Ok(views.html.home.form(form))
}