package services.actors

import _root_.support.datetime.Clock
import akka.actor.{ActorRef, Actor}
import java.util.UUID
import concurrent.duration._

import akka.pattern.ask
import support.{EventsourcedProcessorsProvider, ActorProvider}
import org.eligosource.eventsourced.core.Message

class AuthenticationActor extends Actor {
  provider: Actor with ActorProvider with EventsourcedProcessorsProvider with Clock =>

  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  val usersReadActor = provider.actorFor[UsersReadModelActor]
  val usersWriteActor = provider.processorOf[UsersWriteModelActor]
  implicit val timeout = akka.util.Timeout(5 seconds)

  override def receive = {
    case AuthorizationCommand(UsernamePasswordToken(username, password)) ⇒
      usersReadActor ? ReadUser(UserLookup(username, password)) collect readUser(sender)
  }

  private def readUser(replyTo: ActorRef): Receive = {
    case UserNotFound() ⇒
      replyTo ! AuthorizationFailure()

    case UserFound(user) ⇒ {
      val updatedUser = user.update(sessionKey = generateNewSessionKey, lastLoginDate = Some(now))
      val credentials = UserCredentials(updatedUser.sessionKey)
      usersWriteActor ? Message(UpdateUser(updatedUser)) collect userUpdated(replyTo, credentials)
    }
  }

  private def userUpdated(replyTo: ActorRef, credentials: UserCredentials): Receive = {
    case UserUpdated() ⇒
      replyTo ! AuthorizationSuccess(credentials)
  }

  def generateNewSessionKey: String = {
    UUID.randomUUID().toString
  }
}

trait AuthenticationToken

case class UsernamePasswordToken(username: String, password: String) extends AuthenticationToken

case class UserCredentials(sessionKey: String)

case class AuthorizationCommand(token: AuthenticationToken)

case class AuthorizationSuccess(credentials: UserCredentials)

case class AuthorizationFailure()