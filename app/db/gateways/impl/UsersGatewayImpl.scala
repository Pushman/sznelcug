package db.gateways.impl

import db.gateways.UsersGateway
import db.gateways.helpers.FetchAsync
import org.squeryl.PrimitiveTypeMode._

class UsersGatewayImpl extends UsersGateway {

  import play.api.Play.current
  import db.gateways.tables.UserSchema._

  def findUserBy(username: String, password: String) = FetchAsync {
    users.where(u => u.username === username and u.password === password).headOption
  }
}