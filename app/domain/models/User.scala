package domain.models

object User {

  def apply(username: String, password: String) = new User(0, username, password)
}

case class User(id: Long, username: String, password: String)