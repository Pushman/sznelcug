package services.actors

import akka.actor.{ActorRef, Actor}
import java.util.UUID
import domain.models.User

class AuthenticationActor(replyTo: ActorRef) extends Actor {
  provider: ActorProvider =>

  val usersReadActor = provider.actorFor(classOf[UsersReadModelActor])
  val usersWriteActor = provider.actorFor(classOf[UsersWriteModelActor])

  override def receive = {
    case command@AuthorizationCommand(UsernamePasswordToken(username, password)) =>
      usersReadActor ! ReadUser(UserLookup(username, password))

    case UserFound(user) =>
      usersWriteActor ! UpdateUser(newSessionKeyFor(user))

    case UserNotFound(UserLookup(username, password)) =>
      replyTo ! AuthorizationFailure()

    case UserUpdated(u) =>
      replyTo ! AuthorizationSuccess(UserCredentials(u.sessionKey))
  }

  def newSessionKeyFor(user: User) =
    user.update(sessionKey = UUID.randomUUID().toString)
}


trait AuthenticationToken

case class UsernamePasswordToken(username: String, password: String) extends AuthenticationToken

case class UserCredentials(sessionKey: String)

case class AuthorizationCommand(token: AuthenticationToken)

case class AuthorizationSuccess(credentials: UserCredentials)

case class AuthorizationFailure()