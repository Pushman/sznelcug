package domain.models

object User {

  type UserId = Long

  def apply(username: String, password: String) = new User(-1L, username, password, "")
}

import User._

case class User(id: UserId, username: String, password: String, sessionKey: String) {

  def update(username: String = username, password: String = password, sessionKey: String = sessionKey): User =
    User(id, username, password, sessionKey)
}