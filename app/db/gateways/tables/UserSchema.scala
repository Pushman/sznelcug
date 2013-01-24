package db.gateways.tables

import org.squeryl.Schema
import domain.models.User
import org.squeryl.PrimitiveTypeMode._


object UserSchema extends Schema {

  val users = table[User]("User")

  on(users)(b => declare(
    b.username is(unique)
  ))
}