package db.gateways.impl

import db.gateways.UsersGateway
import db.gateways.helpers.FetchAsync
import org.squeryl.PrimitiveTypeMode._
import domain.models.User
import java.sql.Connection
import concurrent.Future

class UsersGatewayImpl extends UsersGateway {

  import play.api.Play.current
  import db.gateways.tables.UserSchema._
  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  override def findBy(username: String) = FetchAsync {
    users.where(u => u.username === username).headOption
  }

  override def findBy(username: String, password: String) = FetchAsync {
    users.where(u => u.username === username and u.password === password).headOption
  }

  override def update(user: User): Future[Connection => Unit] = Future {
    (connection: Connection) => users.update(user)
  }
}