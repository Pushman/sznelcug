package db.gateways

import domain.models.User
import concurrent.Future


trait UsersGateway {

  def findUserOption(user: User): Future[Option[User]]

  def findUserBy(username: String, password: String): Future[Option[User]]
}