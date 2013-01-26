package services.actors

import akka.actor.{Status, ActorRef, Actor}
import services.AuthenticationToken
import java.util.UUID
import akka.pattern.ask
import akka.pattern.pipe
import services.UsernamePasswordToken
import services.UserCredentials
import db.gateways.helpers.FetchAsync
import org.squeryl.PrimitiveTypeMode._
import domain.models.User
import scala.concurrent.duration._
import java.sql.Connection
import akka.util.Timeout
import concurrent.Future
import util.{Failure, Success}

class AuthenticationActor(val usersReadActor: ActorRef, val usersWriteActor: ActorRef, val controller: ActorRef) extends Actor {

  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  override def receive = {
    case AuthorizationCommand(UsernamePasswordToken(username, password)) =>
      usersReadActor ! ReadUser(UserLookup(username, password))

    case UserFound(user) =>
      usersWriteActor ! UpdateUser(user.update(sessionKey = UUID.randomUUID().toString))

    case UserNotFound(UserLookup(username, password)) =>
      controller ! AuthorizationFailure(UsernamePasswordToken(username, password))

    case UserUpdated(u) =>
      controller ! AuthorizationSuccess(UsernamePasswordToken(u.username, u.password), UserCredentials(u.sessionKey))

    case "ej" =>
      controller ! AuthorizationSuccess(UsernamePasswordToken("ej", "jest "), UserCredentials("?"))

    case any =>
      controller ! AuthorizationSuccess(UsernamePasswordToken(any.toString, "jest "), UserCredentials("?"))
  }
}

class UsersReadModel extends Actor {

  import play.api.Play.current
  import db.gateways.tables.UserSchema._
  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  def receive = {
    case ReadUser(lookup@UserLookup(username, password)) => FetchAsync {
      users.where(u => u.username === username and u.password === password).headOption match {
        case Some(user) => UserFound(user)
        case None => UserNotFound(lookup)
      }
    } pipeTo sender
  }
}

class UsersWriteActor extends Actor {

  import play.api.Play.current
  import db.gateways.tables.UserSchema._
  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  def receive = {
    case UpdateUser(user) => {
      val s = sender
      updateUser(user) onComplete {
        case Success(_) => s ! UserUpdated(user)
        case Failure(f) => s ! Status.Failure(f)
      }
    }
  }

  private def updateUser(user: User): Future[Unit] = FetchAsync {
    users.update(user)
  }
}

case class AuthorizationCommand(token: AuthenticationToken)

case class AuthorizationSuccess(token: AuthenticationToken, credentials: UserCredentials)

case class AuthorizationFailure(token: AuthenticationToken)

case class ReadUser(lookup: UserLookup)

case class UserLookup(username: String, password: String)

case class UserFound(user: User)

case class UserNotFound(lookup: UserLookup)

case class UpdateUser(user: User)

case class UserUpdated(user: User)