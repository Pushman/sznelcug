package domain.models

import User._
import org.joda.time.DateTime

object User {

  type UserId = Long

  def apply(username: String, password: String) = new User(-1L, username, password, "", None)
}


case class User(id: UserId, username: String, password: String, sessionKey: String, lastLoginDate: Option[DateTime]) {

  def update(username: String = username, password: String = password, sessionKey: String = sessionKey, lastLoginDate: Option[DateTime] = lastLoginDate) =
    User(id, username, password, sessionKey, lastLoginDate)
}