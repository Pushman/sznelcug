package services.actors

import akka.actor.{ActorRef, Actor}
import java.util.UUID
import domain.models.User
import concurrent.duration._

import akka.pattern.ask

class AuthenticationActor extends Actor {
  provider: Actor with ActorProvider =>

  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  val usersReadActor = provider.actorFor(classOf[UsersReadModelActor])
  val usersWriteActor = provider.actorFor(classOf[UsersWriteModelActor])
  implicit val timeout = akka.util.Timeout(5 seconds)

  override def receive = {
    case AuthorizationCommand(UsernamePasswordToken(username, password)) =>
      usersReadActor ? ReadUser(UserLookup(username, password)) collect readUser(sender)
  }

  private def readUser(replyTo: ActorRef): Receive = {
    case UserNotFound(_) =>
      replyTo ! AuthorizationFailure()

    case UserFound(user) =>
      usersWriteActor ? UpdateUser(newSessionKeyFor(user)) collect userUpdated(replyTo)
  }

  private def userUpdated(replyTo: ActorRef): Receive = {
    case UserUpdated(user) =>
      replyTo ! AuthorizationSuccess(UserCredentials(user.sessionKey))
  }

  private def newSessionKeyFor(user: User) =
    user.update(sessionKey = UUID.randomUUID().toString)
}

trait AuthenticationToken

case class UsernamePasswordToken(username: String, password: String) extends AuthenticationToken

case class UserCredentials(sessionKey: String)

case class AuthorizationCommand(token: AuthenticationToken)

case class AuthorizationSuccess(credentials: UserCredentials)

case class AuthorizationFailure()