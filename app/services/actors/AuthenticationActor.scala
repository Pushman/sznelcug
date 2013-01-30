package services.actors

import akka.actor.{ActorRef, Actor}
import java.util.UUID
import domain.models.User
import helpers.ChildDispatcher
import concurrent.duration._

class AuthenticationActor extends Actor with ChildDispatcher {
  provider: Actor with ActorProvider =>

  val usersReadActor = provider.actorFor(classOf[UsersReadModelActor])
  val usersWriteActor = provider.actorFor(classOf[UsersWriteModelActor])
  implicit val timeout = (5 seconds)

  override def receive = dispatchToChild(AuthenticationActorChild.apply)

  case class AuthenticationActorChild(replyTo: ActorRef) extends Actor {

    override def receive = {
      case AuthorizationCommand(UsernamePasswordToken(username, password)) =>
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

}

trait AuthenticationToken

case class UsernamePasswordToken(username: String, password: String) extends AuthenticationToken

case class UserCredentials(sessionKey: String)

case class AuthorizationCommand(token: AuthenticationToken)

case class AuthorizationSuccess(credentials: UserCredentials)

case class AuthorizationFailure()