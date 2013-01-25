package db.gateways

import domain.models.User
import concurrent.Future
import java.sql.Connection
import org.squeryl.KeyedEntity

trait UsersGateway {

  def findBy(username: String): Future[Option[User]]
  
  def findBy(username: String, password: String): Future[Option[User]]

  def update(user: User): Future[Connection => Unit]
}