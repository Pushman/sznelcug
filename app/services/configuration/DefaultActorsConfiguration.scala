package services.configuration

import services.actors.support._
import akka.routing.RoundRobinRouter
import services.actors.{AuthenticationActor, UsersWriteModelActor, UsersReadModelActor, ServicesActor}
import akka.actor.Props
import org.eligosource.eventsourced.core.{Eventsourced, Receiver}
import services.actors.support.ActorDetails
import scala.Some
import concurrent.stm.Ref
import domain.models.User

trait DefaultActorsConfiguration extends ActorDetailsActorsConfiguration with MapActorsConfiguration[ActorDetails] {
  this: HasSystem =>

  private def defaultRouter: RoundRobinRouter = new RoundRobinRouter(1)

  private lazy val users = Ref(Vector(User("Admin", "admin")))

  override def actorDetailsMap = {
    Map(
      (classOf[ServicesActor] -> ActorDetails("services", Some("services"),
        Props(new ServicesActor with DefaultActorProvider {
          lazy val system = DefaultActorsConfiguration.this.system
        }))),
      (classOf[UsersReadModelActor] -> ActorDetails("/user/services/usersReadModelActor", Some("usersReadModelActor"),
        Props(new UsersReadModelActor(users.single)).withRouter(defaultRouter))),
      (classOf[UsersWriteModelActor] -> ActorDetails("/user/services/usersWriteModelActor", Some("usersWriteModelActor"),
        Props(new UsersWriteModelActor with Receiver with Eventsourced {
          val id = 1
        }))), (classOf[AuthenticationActor] -> ActorDetails("/user/services/authenticationActor", Some("authenticationActor"),
        Props(new AuthenticationActor with DefaultActorProvider with DefaultEventsourcedProcessorsProvider {
          lazy val system = DefaultActorsConfiguration.this.system
        })))
    )
  }
}