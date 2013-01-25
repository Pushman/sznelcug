package domain.models

import org.squeryl.KeyedEntity

object User {

  def apply(username: String, password: String) = new User(0, username, password, "")
}

case class User(id: Long, username: String, password: String, sessionKey: String) extends KeyedEntity[Long] {

  def update(username: String = username, password: String = password, sessionKey: String = sessionKey): User =
    User(id, username, password, sessionKey)
}