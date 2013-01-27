package services.actors

import akka.actor.{Status, Actor}
import util.{Failure, Success}
import domain.models.User
import concurrent.Future
import db.gateways.helpers.FetchAsync

class UsersWriteModelActor extends Actor {

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

case class UpdateUser(user: User)

case class UserUpdated(user: User)