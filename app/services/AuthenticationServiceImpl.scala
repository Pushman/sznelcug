package services

import db.gateways.UsersGateway
import akka.actor.{TypedActor, TypedProps}
import db.gateways.impl.UsersGatewayImpl
import java.util.UUID
import concurrent.Promise

class AuthenticationServiceImpl extends AuthenticationService {

  import play.api.Play.current
  import play.api.libs.concurrent.Akka
  import play.api.libs.concurrent.Execution.Implicits._

  val usersGateway: UsersGateway = TypedActor(Akka.system).typedActorOf(TypedProps[UsersGatewayImpl]())

  def authenticate(authenticationToken: AuthenticationToken) = authenticationToken match {
    case UsernamePasswordToken(username, password) =>
      usersGateway.findBy(username = username, password = password).map(userOption => userOption.isDefined)
  }

  def authorize(authenticationToken: AuthenticationToken) =
    Promise.successful(UserCredentials(UUID.randomUUID().toString)).future

  def logout(userCredentials: UserCredentials) = ???
}
