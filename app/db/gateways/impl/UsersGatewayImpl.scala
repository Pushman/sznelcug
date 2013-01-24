package db.gateways.impl

import concurrent.Future
import domain.models.User
import db.gateways.UsersGateway
import db.gateways.helpers.FetchAsync
import org.squeryl.PrimitiveTypeMode._

class UsersGatewayImpl extends UsersGateway {

  import play.api.Play.current
  import db.gateways.tables.UserSchema._

  override def findUserOption(user: User): Future[Option[User]] = FetchAsync {
    users.where(u => u.username === user.username and u.password === user.password).headOption
  }

  def findUserBy(username: String, password: String) = FetchAsync {
    users.where(u => u.username === username and u.password === password).headOption
  }
}