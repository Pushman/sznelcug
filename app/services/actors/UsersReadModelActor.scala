package services.actors

import akka.actor.Actor
import domain.models.User
import concurrent.stm.Ref.View

class UsersReadModelActor(users: View[Vector[User]]) extends Actor {

  def receive = {
    case ReadUser(lookup) =>
      sender ! readUser(lookup)
  }

  private def readUser(lookup: UserLookup) =
    users() find oneThatMatchesLookup(lookup) match {
      case Some(user) => UserFound(user)
      case None => UserNotFound()
    }

  private def oneThatMatchesLookup(lookup: UserLookup)(user: User) =
    user.username == lookup.username && user.password == lookup.password
}

case class ReadUser(lookup: UserLookup)

case class UserFound(user: User)

case class UserNotFound()

case class UserLookup(username: String, password: String)