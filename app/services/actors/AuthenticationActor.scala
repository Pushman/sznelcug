package services.actors

import akka.actor.{ActorRef, Actor}
import services.AuthenticationToken
import java.util.UUID
import services.UsernamePasswordToken
import services.UserCredentials
import domain.models.User

class AuthenticationActor(val usersReadModel: ActorRef, val usersWriteModel: ActorRef) extends Actor {

  import context.{become, unbecome}

  override def receive = {
    case AuthorizationCommand(UsernamePasswordToken(username, password)) => {
      become(processingAuthentication(sender))
      usersReadModel ! ReadUser(UserLookup(username, password))
    }
  }

  private def processingAuthentication(replyTo: ActorRef): Receive = {
    case UserFound(user) =>
      usersWriteModel ! UpdateUser(newSessionKeyFor(user))

    case UserNotFound(UserLookup(username, password)) => {
      unbecome()
      replyTo ! AuthorizationFailure()
    }

    case UserUpdated(u) => {
      unbecome()
      replyTo ! AuthorizationSuccess(UserCredentials(u.sessionKey))
    }
  }

  def newSessionKeyFor(user: User) =
    user.update(sessionKey = UUID.randomUUID().toString)
}

case class AuthorizationCommand(token: AuthenticationToken)

case class AuthorizationSuccess(credentials: UserCredentials)

case class AuthorizationFailure()