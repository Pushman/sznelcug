package db.gateways.helpers

import java.sql.Connection
import org.squeryl.{Session, SessionFactory}
import org.squeryl.adapters.H2Adapter

class H2SessionFactory(connection: Connection) extends SessionFactory{

  override def newSession = new Session(connection, new H2Adapter)
}