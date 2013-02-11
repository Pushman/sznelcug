package services.actors

import akka.actor.Actor
import domain.models.User
import domain.models.User.UserId

class UsersWriteModelActor extends Actor {

  private var users: Map[UserId, User] = Map()

  def receive = {
    case UpdateUser(user) => {
      updateUser(user)
      sender ! UserUpdated()
    }
  }

  private def updateUser(user: User) {
    users = users + (user.id -> user)
  }
}

case class UpdateUser(user: User)

case class UserUpdated()