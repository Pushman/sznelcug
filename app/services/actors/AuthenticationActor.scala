package services.actors

import akka.actor.{ActorRef, Actor}
import services.AuthenticationToken
import java.util.UUID
import services.UsernamePasswordToken
import services.UserCredentials

class AuthenticationActor(val usersReadModel: ActorRef, val usersWriteModel: ActorRef, val controller: ActorRef) extends Actor {

  override def receive = {
    case AuthorizationCommand(UsernamePasswordToken(username, password)) =>
      usersReadModel ! ReadUser(UserLookup(username, password))

    case UserFound(user) =>
      usersWriteModel ! UpdateUser(user.update(sessionKey = UUID.randomUUID().toString))

    case UserNotFound(UserLookup(username, password)) =>
      controller ! AuthorizationFailure()

    case UserUpdated(u) =>
      controller ! AuthorizationSuccess(UserCredentials(u.sessionKey))
  }
}

case class AuthorizationCommand(token: AuthenticationToken)

case class AuthorizationSuccess(credentials: UserCredentials)

case class AuthorizationFailure()