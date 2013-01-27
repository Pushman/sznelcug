package services.actors

import akka.actor.{ActorRef, Actor}
import java.util.UUID
import domain.models.User
import play.api.libs.concurrent.Akka

class AuthenticationActor extends Actor {

  import play.api.Play.current
  import context.{become, unbecome}
  
  val usersReadActor = Akka.system.actorFor("user/services/usersReadModelActor")
  val usersWriteActor = Akka.system.actorFor("user/services/usersWriteModelActor")
  
  override def receive = {
    case command@AuthorizationCommand(UsernamePasswordToken(username, password)) => {
      become(processingAuthentication(sender))
      usersReadActor ! ReadUser(UserLookup(username, password))
    }
  }

  private def processingAuthentication(replyTo: ActorRef): Receive = {
    case UserFound(user) =>
      usersWriteActor ! UpdateUser(newSessionKeyFor(user))

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


trait AuthenticationToken

case class UsernamePasswordToken(username: String, password: String) extends AuthenticationToken

case class UserCredentials(sessionKey: String)

case class AuthorizationCommand(token: AuthenticationToken)

case class AuthorizationSuccess(credentials: UserCredentials)

case class AuthorizationFailure()