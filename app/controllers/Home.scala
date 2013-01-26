package controllers

import helpers.AsyncAction
import helpers.AsyncAction._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import akka.actor._
import services.{AuthenticationServiceImpl, AuthenticationService}
import concurrent.{Future, Promise}
import db.gateways.UsersGateway
import db.gateways.impl.UsersGatewayImpl
import db.gateways.helpers.FetchAsync
import services.actors._
import akka.pattern.ask
import services.UsernamePasswordToken
import services.actors.AuthorizationCommand
import services.UserCredentials
import akka.util.Timeout
import scala.concurrent.duration._
import services.UsernamePasswordToken
import services.actors.AuthorizationSuccess
import services.actors.AuthorizationCommand
import services.actors.AuthorizationFailure
import services.UserCredentials

object Home extends Controller {

  import play.api.Play.current
  import play.api.libs.concurrent.Akka
  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  private val authenticationService: AuthenticationService =
    TypedActor(Akka.system).typedActorOf(TypedProps[AuthenticationServiceImpl]())
  private val usersGateway: UsersGateway = TypedActor(Akka.system).typedActorOf(TypedProps[UsersGatewayImpl]())

  private val usersReadActor = Akka.system.actorOf(Props[UsersReadModel]())
  private val usersWriteActor = Akka.system.actorOf(Props[UsersWriteActor]())

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
    val promise = Promise[Result]()
    Akka.system.actorOf(Props(new ControllerActor(promise))) ! AuthorizationCommand(token)
    val asdf = promise.future
    asdf.onComplete({
      case _ =>
        new AuthenticationActor(null, null, null)
    })
    asdf
  }

  class ControllerActor(promise: Promise[Result]) extends Actor {

    def receive = {
      case command: AuthorizationCommand =>
        Akka.system.actorOf(Props(new AuthenticationActor(usersReadActor, usersWriteActor, self))) ! command
      case AuthorizationFailure(t: UsernamePasswordToken) =>
        promise.success(loginFormView(userForm.fill(t).withGlobalError("User invalid")))
      case AuthorizationSuccess(t: UsernamePasswordToken, userCredentials) =>
        promise.success(loginFormView(userForm.fill(t)).withSession("sessionKey" -> userCredentials.sessionKey))
    }
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


  private def updateUserSessionKey(token: UsernamePasswordToken, userCredentials: UserCredentials) = for {
    userOption <- usersGateway.findBy(username = token.username)
    userUpdate <- usersGateway.update(userOption.get.update(sessionKey = userCredentials.sessionKey))
  } yield FetchAsync(userUpdate)


  def loginFormView(form: Form[UsernamePasswordToken]) =
    Ok(views.html.home.form(form))
}