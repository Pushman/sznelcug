package db.gateways.helpers

import java.sql.Connection
import concurrent.Future
import play.api.db.DB
import play.api.libs.concurrent.Execution.Implicits._
import org.squeryl.PrimitiveTypeMode._

object FetchAsync {

  def apply[A](body: Connection => A)(implicit app: play.api.Application): Future[A] = Future {
    DB.withConnection {
      connection => body(connection)
    }
  }

  def apply[A](body: => A)(implicit app: play.api.Application): Future[A] = FetchAsync {
    connection => {
      inTransaction(new H2SessionFactory(connection)) {
        body
      }
    }
  }
}