package services.actors.support

import akka.actor.{Props, Actor}
import akka.routing.RoundRobinRouter
import services.actors.{AuthenticationActor, UsersWriteModelActor, UsersReadModelActor, ServicesActor}
import org.eligosource.eventsourced.core.{Receiver, Eventsourced}

trait ActorsConfiguration[A] {

  def actorConfiguration(clazz: Class[_ <: Actor]): Option[A]
}

trait MapActorsConfiguration[A] extends ActorsConfiguration[A] {

  def actorDetailsMap: Map[Class[_ <: Actor], A]

  override def actorConfiguration(clazz: Class[_ <: Actor]): Option[A] = actorDetailsMap.get(clazz)
}

case class ActorDetails(path: String, name: Option[String], props: Props)

trait ActorDetailsActorsConfiguration extends ActorsConfiguration[ActorDetails] {

  def nameFor = getFromConfiguration(_.name) _

  def pathFor = getFromConfiguration(_.path) _

  def propsFor = getFromConfiguration(_.props) _

  private def getFromConfiguration[A](mapper: ActorDetails => A)(clazz: Class[_ <: Actor]): A =
    mapper(actorConfiguration(clazz).getOrElse(throw new IllegalStateException(clazz.toString)))
}

trait DefaultActorsConfiguration extends ActorDetailsActorsConfiguration with MapActorsConfiguration[ActorDetails] {
  this: HasSystem =>

  private def defaultRouter: RoundRobinRouter = new RoundRobinRouter(1)

  override def actorDetailsMap = {
    Map(
      (classOf[ServicesActor] -> ActorDetails("services", Some("services"),
        Props(new ServicesActor with DefaultActorProvider {
          lazy val system = DefaultActorsConfiguration.this.system
        }))),
      (classOf[UsersReadModelActor] -> ActorDetails("/user/services/usersReadModelActor", Some("usersReadModelActor"),
        Props(new UsersReadModelActor).withRouter(defaultRouter))),
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