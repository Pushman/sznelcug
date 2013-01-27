package services.actors

import akka.pattern.pipe
import org.squeryl.PrimitiveTypeMode._
import akka.actor.Actor
import domain.models.User
import db.gateways.helpers.FetchAsync

class UsersReadModelActor extends Actor {

  import play.api.Play.current
  import db.gateways.tables.UserSchema._
  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  def receive = {
    case ReadUser(lookup@UserLookup(username, password)) => 
      readUser(lookup) pipeTo sender
  }

  def readUser(lookup: UserLookup) = FetchAsync {
    users.where(u => u.username === lookup.username and u.password === lookup.password).headOption match {
      case Some(user) => UserFound(user)
      case None => UserNotFound(lookup)
    }
  }
}

case class ReadUser(lookup: UserLookup)

case class UserFound(user: User)

case class UserNotFound(lookup: UserLookup)

case class UserLookup(username: String, password: String)